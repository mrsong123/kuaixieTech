package com.kuaixie.market;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.apache.ibatis.session.Configuration;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class FinanceKernelTest {
  FinanceKernel k;
  DataStore db;
  FinancePolicy p;
  final java.util.concurrent.atomic.AtomicInteger completedOrderLocks =
      new java.util.concurrent.atomic.AtomicInteger();
  javax.sql.DataSource ds;
  static final FinanceKernel.Actor ADMIN =
      new FinanceKernel.Actor(
          "admin", Set.of("manage", "release", "adjust", "approve_refund", "assess", "provider"));
  static final FinanceKernel.Decision GOOD = new FinanceKernel.Decision(true, true, true, true);

  @BeforeEach
  void setup() throws Exception {
    String mysql = System.getProperty("finance.test.jdbc");
    if (mysql == null) {
      var h2 = new JdbcDataSource();
      h2.setURL(
          "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
      ds = h2;
    } else {
      if (!mysql.matches("jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/finance_test.*"))
        throw new IllegalArgumentException("isolated finance_test schema required");
      var mysqlDs =
          new org.springframework.jdbc.datasource.DriverManagerDataSource(mysql, "root", "");
      String level = System.getProperty("finance.test.isolation", "REPEATABLE READ");
      if (!Set.of("REPEATABLE READ", "READ COMMITTED").contains(level))
        throw new IllegalArgumentException();
      var props = new java.util.Properties();
      props.setProperty(
          "sessionVariables", "transaction_isolation='" + level.replace(" ", "-") + "'");
      mysqlDs.setConnectionProperties(props);
      ds = mysqlDs;
      try (var connection = ds.getConnection();
          var statement = connection.createStatement()) {
        try (var check = statement.executeQuery("SELECT @@transaction_isolation")) {
          assertTrue(check.next());
          assertEquals(level.replace(" ", "-"), check.getString(1));
        }

        for (String file :
            List.of("db/migrate-finance-v7.sql", "db/migrate-finance-governance-v7.sql"))
          for (String line : java.nio.file.Files.readAllLines(java.nio.file.Path.of(file)))
            if (line.startsWith("CREATE TABLE IF NOT EXISTS "))
              statement.execute("DROP TABLE IF EXISTS " + line.split(" ")[5]);
      }
    }
    new ResourceDatabasePopulator(
            new FileSystemResource("db/migrate-finance-v7.sql"),
            new FileSystemResource("db/migrate-finance-governance-v7.sql"))
        .execute(ds);
    Configuration c = new Configuration();
    c.addMapper(LegacySqlMapper.class);
    c.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
    var factory = new SqlSessionFactoryBean();
    factory.setDataSource(ds);
    factory.setConfiguration(c);
    var template = new SqlSessionTemplate(factory.getObject());
    db =
        new DataStore(template.getMapper(LegacySqlMapper.class)) {
          @Override
          public List<Map<String, Object>> queryForList(String sql, Object... args) {
            var result = super.queryForList(sql, args);
            if (sql.contains("fin_order") && sql.contains("FOR UPDATE"))
              completedOrderLocks.incrementAndGet();
            return result;
          }
        };
    p = new FinancePolicy();
    k = new FinanceKernel(db, new DataSourceTransactionManager(ds), p);
    k.initialize("o", "CNY", 1000, "v1");
  }

  long value(String key) {
    return ((Number) k.balance("o").get(key)).longValue();
  }

  String cmd() {
    return UUID.randomUUID().toString();
  }

  void paid(long amount) {
    k.fact("o", cmd(), ADMIN, "payment", "payment", "consideration", amount, "CNY");
  }

  void source() {
    k.source("o", cmd(), ADMIN, "s", 1);
  }

  void block(String id, String scope) {
    k.block("o", cmd(), ADMIN, id, "case", scope, "refund", List.of("s"));
  }

  @Test
  void minimal1OnlyQualifiedRowAndSeparateScopes() {
    source();
    block("b1", "payment");
    block("b2", "other");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            k.release(
                "o",
                cmd(),
                ADMIN,
                "b2",
                0,
                k.manifest("o", "b2"),
                new FinanceKernel.Decision(true, false, true, true)));
    k.release("o", cmd(), ADMIN, "b1", 0, k.manifest("o", "b1"), GOOD);
    assertFalse(k.blocked("o", "payment", "refund"));
    assertTrue(k.blocked("o", "other", "refund"));
    assertEquals(
        "active", db.queryForMap("SELECT status FROM fin_block WHERE id='b2'").get("status"));
  }

  @Test
  void minimal2SourceChangedRejectsAndAudits() {
    source();
    block("b1", "payment");
    var m = k.manifest("o", "b1");
    long rev = value("revision");
    k.source("o", cmd(), ADMIN, "s", 2);
    String command = cmd();
    assertThrows(
        IllegalArgumentException.class, () -> k.release("o", command, ADMIN, "b1", 0, m, GOOD));
    assertEquals(rev + 1, value("revision"));
    assertTrue(k.blocked("o", "payment", "refund"));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_audit WHERE command_id=? AND event_kind='failed'",
            Integer.class,
            command));
  }

  @Test
  void minimal3UnknownSuccessNoReplayAndPersistentUnknownEscalates() {
    paid(1100);
    long rev = value("revision");
    String success =
        db.queryForObject("SELECT id FROM fin_command WHERE result_ref='payment'", String.class);
    db.update("INSERT INTO fin_attempt VALUES('lost',?,'o','hash','admin',0)", success);
    db.update("INSERT INTO fin_attempt VALUES('uncertain','unknown-command','o','hash','admin',0)");
    k.clock(Clock.fixed(Instant.ofEpochMilli(1000), ZoneOffset.UTC));
    k.recover();
    assertEquals(rev + 1, value("revision"));
    assertEquals(1100, value("paid"));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_audit WHERE attempt_id='lost' AND event_kind='recovered'",
            Integer.class));
    k.clock(Clock.fixed(Instant.ofEpochMilli(1000 + p.getEscalationMs() + 1), ZoneOffset.UTC));
    k.recover();
    k.recover();
    assertEquals(
        "escalated",
        db.queryForMap("SELECT status FROM fin_case WHERE command_id='unknown-command'")
            .get("status"));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_audit WHERE command_id='unknown-command' AND"
                + " event_kind='escalated'",
            Integer.class));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_outbox", Integer.class));
  }

  @Test
  void minimal4CrashRecoveryDoesNotRunBusiness() {
    db.update("INSERT INTO fin_attempt VALUES('crash','crashed-command','o','hash','admin',0)");
    k.recover();
    k.recover();
    assertEquals(1, value("revision"));
    assertTrue(k.blocked("o", "payment", "refund"));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_command", Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_case WHERE command_id='crashed-command'", Integer.class));
  }

  @Test
  void minimal5AdjustmentNotRefundThenApprovalReservationProvider() {
    paid(1000);
    k.adjust(
        "o",
        cmd(),
        ADMIN,
        "v1",
        "v2",
        List.of(
            new FinanceKernel.Adjustment("ret", "goods", "change", "goods_return", -100),
            new FinanceKernel.Adjustment("ret", "fee", "change", "fee_return", -20)));
    assertEquals(120, value("excess"));
    assertEquals(0, value("refunded"));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_outbox", Integer.class));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            k.reserve(
                "o",
                cmd(),
                new FinanceKernel.Actor("user", Set.of()),
                "r",
                "payment",
                120,
                value("revision")));
    k.reserve("o", cmd(), ADMIN, "r", "payment", 120, value("revision"));
    assertEquals(120, value("reserved"));
    assertEquals(0, value("refunded"));
    k.refundResult("o", cmd(), ADMIN, "r", 120, 0);
    assertEquals(120, value("refunded"));
    assertEquals(0, value("reserved"));
    assertEquals(0, value("excess"));
  }

  @Test
  void minimal6ReadsDoNotRevise() {
    long before = value("revision");
    for (int i = 0; i < 10; i++) k.balance("o");
    assertEquals(before, value("revision"));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_snapshot", Integer.class));
  }

  @Test
  void minimal7InsufficientEvidenceNoRulingAndAdHocRatioDenied() {
    k.milestone("o", cmd(), ADMIN, "fixed", "stage", 100);
    long fee = value("current_amount");
    k.evidence("o", cmd(), ADMIN, "fixed", "stage", "receipt", false, null);
    assertEquals(
        "needs_evidence", db.queryForMap("SELECT decision FROM fin_milestone").get("decision"));
    assertEquals(fee, value("current_amount"));
    long rev = value("revision");
    assertThrows(
        IllegalArgumentException.class,
        () -> k.evidence("o", cmd(), ADMIN, "fixed", "stage", "receipt", true, 70));
    assertEquals(rev, value("revision"));
    assertEquals(0, value("refunded"));
  }

  @Test
  void partialFailureRetainsUnknownAndOnlyUncoveredRemains() {
    paid(1030);
    k.reserve("o", cmd(), ADMIN, "r", "payment", 30, value("revision"));
    k.refundResult("o", cmd(), ADMIN, "r", 12, 8);
    assertEquals(12, value("refunded"));
    assertEquals(10, value("reserved"));
    assertEquals(18, value("excess"));
    assertThrows(
        IllegalArgumentException.class,
        () -> k.reserve("o", cmd(), ADMIN, "r2", "payment", 18, value("revision")));
    k.reserve("o", cmd(), ADMIN, "r3", "payment", 8, value("revision"));
    assertEquals(18, value("reserved"));
  }

  @Test
  void wholeAdjustmentRollsBackAndIndependentFailureSurvives() {
    String c = cmd();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            k.adjust(
                "o",
                c,
                ADMIN,
                "v1",
                "v2",
                List.of(
                    new FinanceKernel.Adjustment("r", "g", "c", "goods_return", -100),
                    new FinanceKernel.Adjustment("r", "f", "c", "fee_return", 2))));
    assertEquals(0, value("revision"));
    assertEquals(1000, value("current_amount"));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_adjustment", Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_audit WHERE command_id=? AND event_kind='failed'",
            Integer.class,
            c));
  }

  @Test
  void concurrentReservationsCannotOverdraw() throws Exception {
    paid(1030);
    long rev = value("revision");
    var pool = Executors.newFixedThreadPool(2);
    var gate = new CountDownLatch(1);
    try {
      List<Future<Boolean>> f = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        final String id = "r" + i;
        f.add(
            pool.submit(
                () -> {
                  gate.await();
                  try {
                    k.reserve("o", cmd(), ADMIN, id, "payment", 30, rev);
                    return true;
                  } catch (FinanceGuardException e) {
                    assertEquals("STALE_BALANCE", e.code());
                    System.out.println(
                        "MECHANISM|refund_race|order_row_serialization_then_" + e.code());
                    return false;
                  }
                }));
      }
      gate.countDown();
      int ok = 0;
      for (var x : f) if (x.get()) ok++;
      assertEquals(1, ok);
      assertEquals(30, value("reserved"));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void sameCommandReturnsOriginalAndChangedPayloadDenied() {
    String c = cmd();
    var first = k.source("o", c, ADMIN, "s", 1);
    var again = k.source("o", c, ADMIN, "s", 1);
    assertEquals(first, again);
    assertEquals(1, value("revision"));
    assertThrows(IllegalArgumentException.class, () -> k.source("o", c, ADMIN, "s", 2));
  }

  @Test
  void snapshotAndBusinessIdentityDoNotDoubleCount() {
    k.adjust(
        "o",
        cmd(),
        ADMIN,
        "v1",
        "v2",
        List.of(new FinanceKernel.Adjustment("r", "g", "c", "goods_return", -100)));
    assertThrows(
        RuntimeException.class,
        () ->
            k.adjust(
                "o",
                cmd(),
                ADMIN,
                "v2",
                "v3",
                List.of(new FinanceKernel.Adjustment("r", "g", "c", "goods_return", -100))));
    assertEquals(900, value("current_amount"));
    assertEquals(1, value("revision"));
  }

  @Test
  void feePolicyTaxSecurityAndPostageIsolation() {
    assertEquals(-360, p.refundFeeDelta(1800, 72000, 200));
    assertEquals(0, p.refundFeeDelta(100, 10000, 500));
    k.taxEstimate("o", cmd(), ADMIN, 300);
    k.fact("o", cmd(), ADMIN, "hold", "security_hold", "buyer_security", 200, "CNY");
    assertEquals(0, value("paid"));
    assertEquals(1000, value("current_amount"));
    k.postage("o", cmd(), ADMIN, "p", "origin", "buyer", 10000, 100, 50, 0);
    k.expirePostage("o", cmd(), ADMIN, "p");
    assertEquals(1000, value("current_amount"));
    assertEquals("not_agreed", db.queryForMap("SELECT status FROM fin_postage").get("status"));
    assertTrue(p.score(.5, .8, 0, 0) > 0);
  }

  @Test
  void differentCommandsSameEconomicEffectDoNotReviseTwice() {
    paid(1100);
    long r = value("revision");
    k.fact("o", cmd(), ADMIN, "payment", "payment", "consideration", 1100, "CNY");
    assertEquals(r, value("revision"));
    assertEquals(1100, value("paid"));
    assertThrows(
        IllegalArgumentException.class,
        () -> k.fact("o", cmd(), ADMIN, "payment", "payment", "consideration", 1101, "CNY"));
  }

  @Test
  void sourceManifestMissingMemberAndSameCountReplacementAreRejected() {
    source();
    k.source("o", cmd(), ADMIN, "s2", 1);
    k.block("o", cmd(), ADMIN, "b", "case", "payment", "refund", List.of("s", "s2"));
    var m = k.manifest("o", "b");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            k.release(
                "o",
                cmd(),
                ADMIN,
                "b",
                0,
                new FinanceKernel.Manifest(
                    m.moneyRevision(), m.graphRevision(), List.of(m.sources().get(0))),
                GOOD));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            k.release(
                "o",
                cmd(),
                ADMIN,
                "b",
                0,
                new FinanceKernel.Manifest(
                    m.moneyRevision(),
                    m.graphRevision(),
                    List.of(m.sources().get(0), new FinanceKernel.SourceVersion("wrong", 1))),
                GOOD));
    assertTrue(k.blocked("o", "payment", "refund"));
  }

  @Test
  void configurationDefaultsAreNotBusinessApproval() {
    assertFalse(p.isBusinessApproved());
    assertFalse(p.isRecoveryEnabled());
    assertEquals(300, p.taxPlanFeeDelta(10000, "A"));
    assertEquals(-200, p.taxPlanFeeDelta(10000, "B"));
    assertTrue(p.newcomerEligible(true, true, 1000, 0));
    assertFalse(p.newcomerEligible(true, true, p.getNewcomerLimit() + 1, 0));
    assertFalse(p.newcomerEligible(false, true, 1000, 0));
    assertFalse(p.newcomerEligible(true, true, 1000, 1));
  }

  @Test
  void migrationCanBeAppliedTwiceWithoutDestroyingRows() {
    new ResourceDatabasePopulator(
            new FileSystemResource("db/migrate-finance-v7.sql"),
            new FileSystemResource("db/migrate-finance-governance-v7.sql"))
        .execute(ds);
    assertEquals(1000, value("current_amount"));
  }

  @Test
  void threeSourceManifestRejectsSingleChangedMember() {
    source();
    k.source("o", cmd(), ADMIN, "second", 1);
    k.source("o", cmd(), ADMIN, "third", 1);
    k.block(
        "o", cmd(), ADMIN, "triple", "case", "payment", "refund", List.of("s", "second", "third"));
    var old = k.manifest("o", "triple");
    k.source("o", cmd(), ADMIN, "second", 2);
    String request = cmd();
    var rejection =
        assertThrows(
            FinanceGuardException.class,
            () -> k.release("o", request, ADMIN, "triple", 0, old, GOOD));
    assertEquals("STALE_MANIFEST", rejection.code());
    System.out.println("MECHANISM|three_sources|" + rejection.code());
    assertTrue(k.blocked("o", "payment", "refund"));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_audit WHERE command_id=? AND event_kind='failed'",
            Integer.class,
            request));
  }

  @Test
  void outsideUnknownScopeStillRequiresAllFundsGuards() {
    paid(1030);
    k.reserve("o", cmd(), ADMIN, "r", "payment", 30, value("revision"));
    k.refundResult("o", cmd(), ADMIN, "r", 12, 8);
    long revision = value("revision");
    assertFalse(k.blocked("o", "payment", "refund"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            k.reserve(
                "o",
                cmd(),
                new FinanceKernel.Actor("user", Set.of()),
                "unauthorized",
                "payment",
                8,
                revision));
    assertThrows(
        IllegalArgumentException.class,
        () -> k.reserve("o", cmd(), ADMIN, "stale", "payment", 8, revision - 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> k.reserve("o", cmd(), ADMIN, "excess", "payment", 9, revision));
    assertThrows(
        IllegalArgumentException.class,
        () -> k.reserve("o", cmd(), ADMIN, "wrong-source", "missing", 8, revision));
    assertEquals(10, value("reserved"));
    k.reserve("o", cmd(), ADMIN, "valid", "payment", 8, revision);
    assertEquals(18, value("reserved"));
  }

  @Test
  void legacyWalletWriteHelpersRefuseBeforeDatabaseMutation() {
    var database = new DataStore(null);
    var api = new MarketApi(database, "test", "disabled", "test", false, false, null, null, 1, new MatchingFulfillment(database, new DataSourceTransactionManager(new org.h2.jdbcx.JdbcDataSource())), null);
    for (String method : List.of("freezeWallet", "creditWallet")) {
      var e =
          assertThrows(
              org.springframework.web.server.ResponseStatusException.class,
              () ->
                  org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                      api, method, "user", "ref", java.math.BigDecimal.ONE, "test", "test"));
      assertEquals(428, e.getStatusCode().value());
    }
    // A database access would fail because the mapper is absent; both guards reject first.
  }

  @Test
  void sameSourceEffectCannotBeRelabeledGoodsAndFee() {
    k.adjust(
        "o",
        cmd(),
        ADMIN,
        "v1",
        "v2",
        List.of(new FinanceKernel.Adjustment("return", "line", "change", "goods_return", -100)));
    var rejection =
        assertThrows(
            RuntimeException.class,
            () ->
                k.adjust(
                    "o",
                    cmd(),
                    ADMIN,
                    "v2",
                    "v3",
                    List.of(
                        new FinanceKernel.Adjustment(
                            "return", "line", "change", "fee_return", -100))));
    Throwable cause = rejection;
    while (!(cause instanceof java.sql.SQLException) && cause.getCause() != null)
      cause = cause.getCause();
    assertInstanceOf(java.sql.SQLException.class, cause);
    var sql = (java.sql.SQLException) cause;
    assertEquals(
        System.getProperty("finance.test.jdbc") == null ? 23505 : 1062, sql.getErrorCode());
    System.out.println(
        "MECHANISM|economic_unique|sqlstate=" + sql.getSQLState() + ",code=" + sql.getErrorCode());
    assertEquals(900, value("current_amount"));
    assertEquals(0, value("refunded"));
  }

  @Test
  void unsupportedUnlinkedReversalCannotDoubleCountRefund() {
    paid(1030);
    k.reserve("o", cmd(), ADMIN, "refund", "payment", 30, value("revision"));
    k.refundResult("o", cmd(), ADMIN, "refund", 30, 0);
    long revision = value("revision");
    assertThrows(
        IllegalArgumentException.class,
        () -> k.fact("o", cmd(), ADMIN, "refund", "reversal", "consideration", 30, "CNY"));
    assertEquals(30, value("refunded"));
    assertEquals(0, value("reductions"));
    assertEquals(revision, value("revision"));
  }

  @Test
  void concurrentRecoveryDoesNotDuplicateCasesOrTailEffects() throws Exception {
    db.update(
        "INSERT INTO fin_attempt VALUES('recovery-race','unknown-race','o','hash','admin',0)");
    var pool = Executors.newFixedThreadPool(2);
    try {
      var a = pool.submit(() -> k.recover());
      var b = pool.submit(() -> k.recover());
      a.get();
      b.get();
      assertTrue(completedOrderLocks.get() >= 2);
      System.out.println(
          "MECHANISM|recovery_race|completed_order_row_locks="
              + completedOrderLocks.get()
              + ",cases=1,no_business_replay");
      assertEquals(
          1,
          db.queryForObject(
              "SELECT COUNT(*) FROM fin_case WHERE command_id='unknown-race'", Integer.class));
      assertEquals(
          1,
          db.queryForObject(
              "SELECT COUNT(*) FROM fin_block WHERE reason='unknown_result'", Integer.class));
      assertEquals(
          0,
          db.queryForObject(
              "SELECT COUNT(*) FROM fin_command WHERE id='unknown-race'", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void databaseLockWaitTimeoutIsReported() throws Exception {
    try (var a = ds.getConnection();
        var b = ds.getConnection()) {
      a.setAutoCommit(false);
      b.setAutoCommit(false);
      try (var sa = a.createStatement();
          var sb = b.createStatement()) {
        if (a.getMetaData().getDatabaseProductName().equals("MySQL"))
          sb.execute("SET SESSION innodb_lock_wait_timeout=1");
        else sb.execute("SET LOCK_TIMEOUT 100");
        sa.executeQuery("SELECT id FROM fin_order WHERE id='o' FOR UPDATE").close();
        var failure =
            assertThrows(
                java.sql.SQLException.class,
                () -> sb.executeQuery("SELECT id FROM fin_order WHERE id='o' FOR UPDATE"));
        System.out.println(
            "MECHANISM|lock_wait|sqlstate="
                + failure.getSQLState()
                + ",code="
                + failure.getErrorCode());
        if (a.getMetaData().getDatabaseProductName().equals("MySQL"))
          assertEquals(1205, failure.getErrorCode());
      } finally {
        a.rollback();
        b.rollback();
      }
    }
    assertEquals(0, value("revision"));
  }

  FinanceGovernance governanceFixture() {
    db.update("DROP TABLE IF EXISTS orders");
    db.update("DROP TABLE IF EXISTS users");
    db.update("CREATE TABLE users(id VARCHAR(80) PRIMARY KEY,status VARCHAR(20),role VARCHAR(20))");
    db.update(
        "CREATE TABLE orders(id VARCHAR(80) PRIMARY KEY,buyer_id VARCHAR(80),vendor_id"
            + " VARCHAR(80))");
    for (String id : List.of("admin", "buyer", "seller", "finance", "outsider"))
      db.update(
          "INSERT INTO users(id,status,role) VALUES(?,'active',?)",
          id,
          id.equals("admin") ? "admin" : "user");
    db.update("INSERT INTO orders VALUES('o','buyer','seller')");
    var g = new FinanceGovernance(db, new DataSourceTransactionManager(ds));
    g.bind("o", "admin", "reviewed-original-contract");
    return g;
  }

  @Test
  void boundContractRequiresSameDigestBothParties() {
    var g = governanceFixture();
    g.grant("admin", "admin", "adjust", true);
    var lines =
        List.of(new FinanceKernel.Adjustment("ret", "goods", "change", "goods_return", -100));
    String digest =
        g.propose("o", "buyer", "v2", "v1", Map.of("currency", "CNY", "reason", "return"), lines);
    assertThrows(FinanceGuardException.class, () -> g.confirm("o", "buyer", "v2", "wrong"));
    assertThrows(FinanceGuardException.class, () -> g.confirm("o", "outsider", "v2", digest));
    g.confirm("o", "buyer", "v2", digest);
    var actor = g.actor("admin");
    assertEquals(
        "BOTH_CONFIRMATIONS_REQUIRED",
        assertThrows(
                FinanceGuardException.class, () -> k.adjust("o", cmd(), actor, "v1", "v2", lines))
            .code());
    g.confirm("o", "seller", "v2", digest);
    k.adjust("o", cmd(), actor, "v1", "v2", lines);
    assertEquals(900, value("current_amount"));
    assertEquals(2, db.queryForObject("SELECT COUNT(*) FROM fin_confirmation", Integer.class));
  }

  @Test
  void boundContractCannotChangeConfirmedContent() {
    var g = governanceFixture();
    g.grant("admin", "admin", "adjust", true);
    var lines =
        List.of(new FinanceKernel.Adjustment("ret", "goods", "change", "goods_return", -100));
    String digest = g.propose("o", "buyer", "v2", "v1", Map.of("reason", "return"), lines);
    for (String party : List.of("buyer", "seller")) g.confirm("o", party, "v2", digest);
    assertEquals(
        "AGREEMENT_CONTENT_MISMATCH",
        assertThrows(
                FinanceGuardException.class,
                () ->
                    k.adjust(
                        "o",
                        cmd(),
                        g.actor("admin"),
                        "v1",
                        "v2",
                        List.of(
                            new FinanceKernel.Adjustment(
                                "ret", "goods", "change", "goods_return", -200))))
            .code());
    assertEquals(1000, value("current_amount"));
    assertEquals(
        "proposed", db.queryForMap("SELECT status FROM fin_agreement WHERE id='v2'").get("status"));
  }

  @Test
  void revokedDatabaseRoleCannotUsePreviouslyCapturedActor() {
    var g = governanceFixture();
    g.grant("admin", "admin", "manage", true);
    var captured = g.actor("admin");
    g.grant("admin", "admin", "manage", false);
    assertEquals(
        "ROLE_PERMISSION_DENIED",
        assertThrows(FinanceGuardException.class, () -> k.source("o", cmd(), captured, "source", 1))
            .code());
    assertEquals(0, value("revision"));
    assertThrows(FinanceGuardException.class, () -> g.grant("buyer", "buyer", "manage", true));
  }

  @Test
  void liveOrderPartyChangeRejectsOldBinding() {
    var g = governanceFixture();
    g.grant("admin", "admin", "manage", true);
    db.update("UPDATE orders SET vendor_id='outsider' WHERE id='o'");
    assertEquals(
        "ORDER_PARTY_CHANGED",
        assertThrows(
                FinanceGuardException.class,
                () -> k.source("o", cmd(), g.actor("admin"), "source", 1))
            .code());
  }

  @Test
  void boundRefundRequiresSeparateVersionBoundApproval() {
    paid(1010);
    var g = governanceFixture();
    g.grant("admin", "buyer", "approve_refund", true);
    g.grant("admin", "finance", "approve_refund", true);
    g.requestRefund("o", "buyer", "review", "payment", 10);
    assertEquals(
        "SELF_APPROVAL_DENIED",
        assertThrows(FinanceGuardException.class, () -> g.approveRefund("o", "buyer", "review"))
            .code());
    g.approveRefund("o", "finance", "review");
    var actor = g.actor("finance");
    long revision = value("revision");
    assertEquals(
        "REFUND_APPROVAL_MISMATCH",
        assertThrows(
                FinanceGuardException.class,
                () -> k.reserve("o", cmd(), actor, "review", "payment", 11, revision))
            .code());
    assertEquals(0, value("reserved"));
    k.reserve("o", cmd(), actor, "review", "payment", 10, revision);
    assertEquals(10, value("reserved"));
    assertEquals(
        "consumed",
        db.queryForMap("SELECT status FROM fin_refund_review WHERE id='review'").get("status"));
  }

  @Test
  void concurrentConfirmedContractsHaveSameConflictGuard() throws Exception {
    var g = governanceFixture();
    g.grant("admin", "admin", "adjust", true);
    var a = List.of(new FinanceKernel.Adjustment("retA", "goods", "changeA", "goods_return", -100));
    var b = List.of(new FinanceKernel.Adjustment("retB", "goods", "changeB", "goods_return", -200));
    for (String version : List.of("v2", "v3")) {
      String digest =
          g.propose(
              "o",
              "buyer",
              version,
              "v1",
              Map.of("version", version),
              version.equals("v2") ? a : b);
      g.confirm("o", "buyer", version, digest);
      g.confirm("o", "seller", version, digest);
    }
    var pool = Executors.newFixedThreadPool(2);
    var start = new CountDownLatch(1);
    try {
      var actor = g.actor("admin");
      var futures = new ArrayList<Future<Boolean>>();
      for (String v : List.of("v2", "v3"))
        futures.add(
            pool.submit(
                () -> {
                  start.await();
                  try {
                    k.adjust("o", cmd(), actor, "v1", v, v.equals("v2") ? a : b);
                    return true;
                  } catch (FinanceGuardException e) {
                    assertEquals("AGREEMENT_PARENT_CONFLICT", e.code());
                    System.out.println(
                        "MECHANISM|contract_race|order_row_serialization_then_" + e.code());
                    return false;
                  }
                }));
      start.countDown();
      int successes = 0;
      for (var f : futures) if (f.get()) successes++;
      assertEquals(1, successes);
      assertEquals(
          1,
          db.queryForObject(
              "SELECT COUNT(*) FROM fin_agreement WHERE status='active'", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void genuineComponentsShareDocumentButRemainDistinct() {
    var lines =
        List.of(
            new FinanceKernel.Adjustment("return", "goods:line1", "change", "goods_return", -100),
            new FinanceKernel.Adjustment("return", "service:purchase", "change", "fee_return", -20),
            new FinanceKernel.Adjustment(
                "return", "service:delivery", "change", "fee_return", -30));
    k.adjust("o", cmd(), ADMIN, "v1", "v2", lines);
    assertEquals(850, value("current_amount"));
    assertEquals(3, db.queryForObject("SELECT COUNT(*) FROM fin_adjustment", Integer.class));
    assertThrows(
        RuntimeException.class,
        () ->
            k.adjust(
                "o",
                cmd(),
                ADMIN,
                "v2",
                "v3",
                List.of(
                    new FinanceKernel.Adjustment(
                        "return", "service:purchase", "change", "other_fee", -20))));
    assertEquals(850, value("current_amount"));
  }

  FinanceGovernanceCommands commands(FinanceGovernance g) {
    return new FinanceGovernanceCommands(db, new DataSourceTransactionManager(ds), g, k);
  }

  @Test
  void journalReusesCommandAndRejectsChangedContent() {
    var g = governanceFixture();
    var c = commands(g);
    c.grant("grant-one", "admin", "finance", "approve_refund", true);
    long events =
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_event WHERE action='grant'", Long.class);
    c.grant("grant-one", "admin", "finance", "approve_refund", true);
    assertEquals(
        events,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_event WHERE action='grant'", Long.class));
    assertEquals(
        "GOVERNANCE_IDEMPOTENCY_CONFLICT",
        assertThrows(
                FinanceGuardException.class,
                () -> c.grant("grant-one", "admin", "finance", "approve_refund", false))
            .code());
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_outcome WHERE kind='failed'", Integer.class));
    assertEquals(
        1, db.queryForObject("SELECT COUNT(*) FROM fin_governance_command", Integer.class));
  }

  @Test
  void deniedGovernanceIsAuditedOutsideRollback() {
    var g = governanceFixture();
    var c = commands(g);
    assertThrows(
        FinanceGuardException.class, () -> c.grant("forbidden", "buyer", "buyer", "manage", true));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_role_grant", Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_attempt WHERE command_id='forbidden'",
            Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_outcome WHERE kind='failed'", Integer.class));
  }

  @Test
  void governanceConcurrentSameCommandHasOneEffect() throws Exception {
    var g = governanceFixture();
    var c = commands(g);
    var pool = Executors.newFixedThreadPool(2);
    var start = new CountDownLatch(1);
    try {
      List<Future<?>> tasks = new ArrayList<>();
      for (int i = 0; i < 2; i++)
        tasks.add(
            pool.submit(
                () -> {
                  start.await();
                  c.grant("same-grant", "admin", "finance", "approve_refund", true);
                  return null;
                }));
      start.countDown();
      for (var f : tasks) f.get();
      assertEquals(
          1,
          db.queryForObject(
              "SELECT COUNT(*) FROM fin_governance_event WHERE action='grant'", Integer.class));
      assertEquals(
          1, db.queryForObject("SELECT COUNT(*) FROM fin_governance_command", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void governanceRecoveryQueriesOnlyAndUsesD01() {
    var g = governanceFixture();
    var c = commands(g);
    db.update(
        "INSERT INTO fin_governance_attempt"
            + " VALUES('crashed-gov','unknown-gov','o','buyer','confirm','hash',0)");
    c.recover();
    c.recover();
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_confirmation", Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_case WHERE command_id='unknown-gov' AND"
                + " target_type='governance'",
            Integer.class));
    db.update(
        "INSERT INTO fin_governance_attempt"
            + " VALUES('global-gov','unknown-global',NULL,'admin','grant','hash',0)");
    c.recover();
    k.clock(
        Clock.fixed(
            Instant.ofEpochMilli(System.currentTimeMillis() + p.getEscalationMs() + 1000),
            ZoneOffset.UTC));
    k.recover();
    assertEquals(
        "escalated",
        db.queryForMap("SELECT status FROM fin_case WHERE command_id='unknown-global'")
            .get("status"));
    assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM fin_role_grant", Integer.class));
  }

  @Test
  void governanceRecoveryFindsCommitWithoutReexecuting() {
    var g = governanceFixture();
    var c = commands(g);
    c.grant("known-gov", "admin", "finance", "approve_refund", true);
    db.update(
        "INSERT INTO"
            + " fin_governance_attempt(id,command_id,order_id,actor,action,payload_hash,accepted_ms)"
            + " SELECT 'lost-gov',command_id,order_id,actor,action,payload_hash,0 FROM"
            + " fin_governance_attempt WHERE command_id='known-gov'");
    c.recover();
    c.recover();
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_event WHERE action='grant'", Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_outcome WHERE attempt_id='lost-gov' AND"
                + " kind='recovered'",
            Integer.class));
  }

  String sessionToken(String user) throws Exception {
    String payload = user + "|" + (System.currentTimeMillis() / 1000 + 3600) + "|session";
    var mac = javax.crypto.Mac.getInstance("HmacSHA256");
    mac.init(
        new javax.crypto.spec.SecretKeySpec(
            "session-test-secret".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
    return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8))
        + "."
        + Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }

  FinanceSessionAdapter sessionFixture(FinanceGovernance g, String token) {
    db.update("DROP TABLE IF EXISTS revoked_tokens");
    db.update("DROP TABLE IF EXISTS auth_sessions");
    db.update("CREATE TABLE revoked_tokens(token_hash VARCHAR(64),expires_at TIMESTAMP)");
    db.update(
        "CREATE TABLE auth_sessions(user_id VARCHAR(80),token_hash VARCHAR(64),revoked_at"
            + " TIMESTAMP,expires_at TIMESTAMP)");
    db.update(
        "INSERT INTO auth_sessions VALUES('buyer',?,NULL,?)",
        RequestAuth.tokenHash(token),
        java.sql.Timestamp.from(Instant.now().plusSeconds(3600)));
    return new FinanceSessionAdapter(new RequestAuth(db, "session-test-secret"), g);
  }

  @Test
  void sessionAdapterIgnoresClaimedActorAndRole() throws Exception {
    var g = governanceFixture();
    String token = sessionToken("buyer");
    var adapter = sessionFixture(g, token);
    var request = new org.springframework.mock.web.MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    request.setParameter("actor", "admin");
    request.setParameter("role", "admin");
    assertEquals("buyer", adapter.authenticatedActor(request).id());
    assertTrue(adapter.authenticatedActor(request).permissions().isEmpty());
  }

  @Test
  void sessionAdapterRejectsRevocationAndForgedToken() throws Exception {
    var g = governanceFixture();
    String token = sessionToken("buyer");
    var adapter = sessionFixture(g, token);
    var request = new org.springframework.mock.web.MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    db.update("UPDATE auth_sessions SET revoked_at=?", java.sql.Timestamp.from(Instant.now()));
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> adapter.authenticatedActor(request));
    var forged = new org.springframework.mock.web.MockHttpServletRequest();
    forged.addHeader("Authorization", "Bearer " + token.substring(0, token.length() - 5) + "WRONG");
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> adapter.authenticatedActor(forged));
  }

  @Test
  void previouslyUnresolvedAttemptsDoNotStarveNewCases() {
    var g = governanceFixture();
    var c = commands(g);
    for (int i = 0; i < 101; i++)
      db.update(
          "INSERT INTO fin_governance_attempt VALUES(?,?,'o','buyer','confirm','hash',?)",
          "attempt" + i,
          "cmd" + i,
          i);
    c.recover();
    assertEquals(100, db.queryForObject("SELECT COUNT(*) FROM fin_case", Integer.class));
    c.recover();
    assertEquals(101, db.queryForObject("SELECT COUNT(*) FROM fin_case", Integer.class));
    assertEquals(
        0, db.queryForObject("SELECT COUNT(*) FROM fin_governance_command", Integer.class));
  }
}
