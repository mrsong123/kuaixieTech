package com.kuaixie.market;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/** Actual embedded Tomcat + TCP HttpClient + real database. No MockMvc or authentication mocks. */
class FinanceHttpTest {
  @Configuration(proxyBeanMethods = false)
  @EnableAutoConfiguration(
      excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
      })
  static class HttpOnlyConfiguration {}

  @org.springframework.web.bind.annotation.RestController
  static class ShellFixtureApi {
    @org.springframework.web.bind.annotation.GetMapping("/account/preferences")
    public Map<String, Object> preferences() {
      return Map.of("locale", "zh-CN");
    }

    @org.springframework.web.bind.annotation.GetMapping("/notifications")
    public Map<String, Object> notices() {
      return Map.of("unreadCount", 0, "items", List.of());
    }
  }

  FinanceKernelTest fixture;
  FinanceGovernance g;
  FinanceGovernanceCommands commands;
  FinancePolicy policy;
  ServletWebServerApplicationContext context;
  String base, digest;
  Map<String, String> tokens = new HashMap<>();
  final ObjectMapper json = new ObjectMapper();
  final java.util.concurrent.ConcurrentLinkedQueue<Map<String, Object>> httpTrace =
      new java.util.concurrent.ConcurrentLinkedQueue<>();
  final HttpClient client =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  @BeforeEach
  void setup() throws Exception {
    fixture = new FinanceKernelTest();
    fixture.setup();
    g = fixture.governanceFixture();
    var tm = new DataSourceTransactionManager(fixture.ds);
    commands = new FinanceGovernanceCommands(fixture.db, tm, g, fixture.k);
    policy = fixture.p;
    policy.setGovernanceHttpEnabled(true);
    g.grant("admin", "finance", "approve_refund", true);
    digest =
        g.propose(
            "o",
            "buyer",
            "v2",
            "v1",
            Map.of("goods", "same confirmed description"),
            List.of(
                new FinanceKernel.Adjustment("ret", "goods:one", "change", "goods_return", -100)));
    var auth = new RequestAuth(fixture.db, "session-test-secret");
    for (String id : List.of("buyer", "seller", "outsider", "finance", "admin"))
      tokens.put(id, fixture.sessionToken(id));
    fixture.sessionFixture(g, tokens.get("buyer"));
    for (String id : List.of("buyer", "seller", "outsider", "finance", "admin")) {
      String token = tokens.get(id);
      if (!id.equals("buyer"))
        fixture.db.update(
            "INSERT INTO auth_sessions VALUES(?,?,NULL,?)",
            id,
            RequestAuth.tokenHash(token),
            java.sql.Timestamp.from(java.time.Instant.now().plusSeconds(3600)));
    }
    var session = new FinanceSessionAdapter(auth, g);
    context =
        (ServletWebServerApplicationContext)
            new SpringApplicationBuilder(HttpOnlyConfiguration.class)
                .initializers(
                    app -> {
                      var c = (GenericApplicationContext) app;
                      c.registerBean("dataSource", javax.sql.DataSource.class, () -> fixture.ds);
                      c.registerBean("requestAuth", RequestAuth.class, () -> auth);
                      c.registerBean("supportApi", SupportApi.class, () -> new SupportApi(fixture.db,auth));
                      c.registerBean(
                          "governanceApi",
                          FinanceGovernanceApi.class,
                          () ->
                              new FinanceGovernanceApi(
                                  g,
                                  commands,
                                  session,
                                  policy,
                                  System.getProperty("finance.test.environment", "development")));
                      c.registerBean(
                          "financeReadApi",
                          FinanceReadApi.class,
                          () -> new FinanceReadApi(fixture.k, auth, policy));
                      c.registerBean(
                          "financeHttpBoundary",
                          FinanceHttpBoundary.class,
                          () -> new FinanceHttpBoundary(auth));
                      c.registerBean(
                          "financeApiErrors", FinanceApiErrors.class, FinanceApiErrors::new);
                      c.registerBean(
                          "shellFixtureApi", ShellFixtureApi.class, ShellFixtureApi::new);
                    })
                .run(
                    "--server.port=0",
                    "--server.address=127.0.0.1",
                    "--server.servlet.context-path=/api",
                    "--server.forward-headers-strategy=NONE",
                    "--spring.sql.init.mode=never",
                    "--logging.level.root=ERROR",
                    "--spring.main.banner-mode=off");
    base = "http://127.0.0.1:" + context.getWebServer().getPort() + "/api";
  }

  @AfterEach
  void recordAndClose(org.junit.jupiter.api.TestInfo info) throws Exception {
    try {
      String env =
          System.getProperty("finance.test.jdbc") == null
              ? "h2"
              : System.getProperty("finance.test.isolation", "REPEATABLE READ")
                  .toLowerCase()
                  .replace(' ', '-');
      var path =
          java.nio.file.Path.of(
              System.getProperty("finance.test.evidence", "../verification/v7-batch5"),
              env,
              "http-trace-" + info.getTestMethod().orElseThrow().getName() + ".json");
      java.nio.file.Files.createDirectories(path.getParent());
      java.nio.file.Files.writeString(
          path, json.writerWithDefaultPrettyPrinter().writeValueAsString(httpTrace));
    } finally {
      close();
    }
  }

  void close() {
    if (context != null) context.close();
  }

  HttpResponse<String> send(
      String method, String path, String user, String key, String body, String... headers)
      throws Exception {
    var b =
        HttpRequest.newBuilder(URI.create(base + path)).timeout(java.time.Duration.ofSeconds(15));
    if (user != null) b.header("Authorization", "Bearer " + tokens.get(user));
    if (key != null) b.header("Idempotency-Key", key);
    for (int i = 0; i < headers.length; i += 2) b.header(headers[i], headers[i + 1]);
    if (body != null) b.header("Content-Type", "application/json");
    b.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));
    var response = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    var names = new ArrayList<String>();
    if (user != null) names.add("Authorization");
    if (key != null) names.add("Idempotency-Key");
    for (int i = 0; i < headers.length; i += 2) names.add(headers[i]);
    String code = "";
    try {
      code = json.readTree(response.body()).path("code").asText();
    } catch (Exception ignored) {
    }
    httpTrace.add(
        Map.of(
            "method",
            method,
            "path",
            path,
            "credentialFixture",
            user == null ? "none-or-explicit-header" : user,
            "headerNames",
            names,
            "status",
            response.statusCode(),
            "responseCode",
            code));
    return response;
  }

  String confirmBody() {
    return "{\"digest\":\"" + digest + "\",\"actor\":\"admin\",\"role\":\"admin\"}";
  }

  String confirmPath() {
    return "/finance/orders/o/agreements/v2/confirm";
  }

  @Test
  void anonymousCookieAndForwardedClaimsCannotAuthenticate() throws Exception {
    var response =
        send(
            "GET",
            "/finance/orders/o/agreement",
            null,
            null,
            null,
            "Cookie",
            "token=" + tokens.get("buyer"),
            "X-User-Id",
            "buyer",
            "X-Role",
            "admin",
            "X-Forwarded-User",
            "buyer",
            "X-Forwarded-For",
            "127.0.0.1");
    assertEquals(401, response.statusCode());
    assertEquals("UNAUTHENTICATED", json.readTree(response.body()).get("code").asText());
    assertEquals(401, send("POST", confirmPath(), null, "x", "not-json").statusCode());
    assertEquals(
        0, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_governance_attempt", Integer.class));
  }

  @Test
  void outsiderCannotReadOrConfirmEvenWhenBodyClaimsBuyer() throws Exception {
    assertEquals(
        403, send("GET", "/finance/orders/o/agreement", "outsider", null, null).statusCode());
    assertEquals(
        403, send("POST", confirmPath(), "outsider", "denied", confirmBody()).statusCode());
    assertEquals(
        0, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_confirmation", Integer.class));
    assertEquals(
        1,
        fixture.db.queryForObject(
            "SELECT COUNT(*) FROM fin_governance_outcome WHERE kind='failed'", Integer.class));
  }

  @Test
  void concurrentHttpRetryConfirmsOnceAndChangedBodyConflicts() throws Exception {
    var read =
        send(
            "GET", "/finance/orders/o/agreement", "buyer", null, null, "X-Forwarded-User", "admin");
    assertEquals(200, read.statusCode());
    assertEquals("buyer", json.readTree(read.body()).get("viewerId").asText());
    var pool = Executors.newFixedThreadPool(2);
    try {
      var a = pool.submit(() -> send("POST", confirmPath(), "buyer", "same", confirmBody()));
      var b = pool.submit(() -> send("POST", confirmPath(), "buyer", "same", confirmBody()));
      assertEquals(200, a.get().statusCode());
      assertEquals(200, b.get().statusCode());
    } finally {
      pool.shutdownNow();
    }
    assertEquals(
        1, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_confirmation", Integer.class));
    assertEquals(
        1, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_governance_command", Integer.class));
    assertEquals(
        "buyer", fixture.db.queryForMap("SELECT user_id FROM fin_confirmation").get("user_id"));
    var conflict =
        send("POST", confirmPath(), "buyer", "same", "{\"digest\":\"" + "0".repeat(64) + "\"}");
    assertEquals(409, conflict.statusCode());
    assertEquals(
        "GOVERNANCE_IDEMPOTENCY_CONFLICT", json.readTree(conflict.body()).get("code").asText());
    assertEquals(1000, fixture.value("current_amount"));
    assertEquals(0, fixture.value("revision"));
    assertEquals(0, fixture.value("refunded"));
  }

  @Test
  void revokedForgedAndDuplicateAuthorizationRejectedOverTcp() throws Exception {
    fixture.db.update(
        "UPDATE auth_sessions SET revoked_at=? WHERE user_id='buyer'",
        java.sql.Timestamp.from(java.time.Instant.now()));
    assertEquals(401, send("GET", "/finance/orders/o/agreement", "buyer", null, null).statusCode());
    assertEquals(
        401,
        send(
                "GET",
                "/finance/orders/o/agreement",
                null,
                null,
                null,
                "Authorization",
                "Bearer invalid")
            .statusCode());
    assertEquals(
        401,
        send(
                "GET",
                "/finance/orders/o/agreement",
                "seller",
                null,
                null,
                "Authorization",
                "Bearer " + tokens.get("admin"))
            .statusCode());
  }

  @Test
  void roleRevocationIsVisibleWithoutReissuingSession() throws Exception {
    assertEquals(
        200, send("GET", "/finance/orders/o/agreement", "finance", null, null).statusCode());
    g.grant("admin", "finance", "approve_refund", false);
    assertEquals(
        403, send("GET", "/finance/orders/o/agreement", "finance", null, null).statusCode());
    assertEquals(
        403, send("GET", "/admin/finance-kernel-policy", "buyer", null, null).statusCode());
    var policyResponse = send("GET", "/admin/finance-kernel-policy", "admin", null, null);
    assertEquals(200, policyResponse.statusCode());
    assertFalse(json.readTree(policyResponse.body()).get("externalExecutionEnabled").asBoolean());
    assertEquals(
        404,
        send("GET", "/admin/finance-kernel/missing/balance", "admin", null, null).statusCode());
    assertEquals(0, fixture.value("revision"));
  }

  @Test
  void validationAndDisabledWriteGateLeaveNoCommand() throws Exception {
    assertEquals(400, send("POST", confirmPath(), "buyer", null, confirmBody()).statusCode());
    assertEquals(
        400,
        send("POST", confirmPath(), "buyer", "valid", "{\"digest\":\"invalid\"}").statusCode());
    policy.setGovernanceHttpEnabled(false);
    assertEquals(503, send("POST", confirmPath(), "buyer", "blocked", confirmBody()).statusCode());
    assertEquals(
        0, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_governance_attempt", Integer.class));
    assertEquals(0, fixture.value("revision"));
  }

  @Test
  void keyNamespaceIsPerAuthenticatedIdentityAndBadDigestIsRejected() throws Exception {
    assertEquals(
        409,
        send(
                "POST",
                confirmPath(),
                "buyer",
                "wrong-first",
                "{\"digest\":\"" + "0".repeat(64) + "\"}")
            .statusCode());
    assertEquals(
        200, send("POST", confirmPath(), "buyer", "shared-key", confirmBody()).statusCode());
    assertEquals(
        200, send("POST", confirmPath(), "seller", "shared-key", confirmBody()).statusCode());
    assertEquals(
        2, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_confirmation", Integer.class));
    assertEquals(
        2, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_governance_command", Integer.class));
    assertEquals("v1", fixture.k.balance("o").get("agreement"));
    assertEquals(0, fixture.value("revision"));
    assertEquals(0, fixture.db.queryForObject("SELECT COUNT(*) FROM fin_outbox", Integer.class));
  }

  @Test
  void productionRejectsConfirmEvenIfDevelopmentSwitchIsTrue() throws Exception {
    context.close();
    try {
      System.setProperty("finance.test.environment", "production");
      setup();
      assertTrue(policy.isGovernanceHttpEnabled());
      assertEquals(
          503,
          send("POST", confirmPath(), "buyer", "production-blocked", confirmBody()).statusCode());
      assertFalse(
          json.readTree(send("GET", "/finance/orders/o/agreement", "buyer", null, null).body())
              .get("confirmationAvailable")
              .asBoolean());
      assertEquals(
          0,
          fixture.db.queryForObject("SELECT COUNT(*) FROM fin_governance_attempt", Integer.class));
    } finally {
      System.clearProperty("finance.test.environment");
    }
  }
}
