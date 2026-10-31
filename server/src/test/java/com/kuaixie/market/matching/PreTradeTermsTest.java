package com.kuaixie.market.matching;

import static org.junit.jupiter.api.Assertions.*;

import com.kuaixie.market.FinancePolicy;
import java.util.*;
import org.junit.jupiter.api.Test;

/** C07 成交前置四选：规则单测（纯逻辑，不依赖数据库）。 */
class PreTradeTermsTest {
  private static Map<String, Object> demand(String scope, String bearer, String tax, int days, String anchor) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("deliveryScope", scope);
    m.put("postageBearer", bearer);
    m.put("taxRefundOwner", tax);
    m.put("leadTimeDays", days);
    m.put("leadTimeAnchor", anchor);
    return m;
  }

  private static Map<String, Object> offer(List<String> scopes, String postage, String tax, int days, String anchor) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("deliveryScopes", scopes);
    m.put("postageMode", postage);
    m.put("taxRefundMode", tax);
    m.put("promiseDays", days);
    m.put("leadTimeAnchor", anchor);
    return m;
  }

  @Test
  void meetupLocksPostageToNone() {
    var n = PreTradeTerms.normalizeDemand(Map.of("deliveryScope", "meetup", "postageBearer", "buyer"), "中国");
    assertEquals("none", n.get("postageBearer"));
    var r = PreTradeTerms.normalizeDemand(Map.of("deliveryScope", "return_then_meetup"), "美国");
    assertEquals("none", r.get("postageBearer"));
  }

  @Test
  void internationalCannotBeMeetupOnly() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PreTradeTerms.normalizeDemand(Map.of("deliveryScope", "meetup"), "日本"));
    assertEquals(
        "international_mail",
        PreTradeTerms.normalizeDemand(Map.of("deliveryScope", "international_mail"), "日本").get("deliveryScope"));
  }

  @Test
  void invalidValuesAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.normalizeDemand(Map.of("deliveryScope", "x"), ""));
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.normalizeDemand(Map.of("postageBearer", "none"), ""));
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.normalizeDemand(Map.of("leadTimeDays", 0), ""));
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.normalizeDemand(Map.of("leadTimeAnchor", "x"), ""));
  }

  @Test
  void submitRequiresAllFourAndNamesWhatIsMissing() {
    var e = assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.requireComplete(Map.of("deliveryScope", "nationwide"), "中国"));
    assertTrue(e.getMessage().contains("邮费") && e.getMessage().contains("退税") && e.getMessage().contains("时效"));
    assertFalse(e.getMessage().contains("交付范围"));
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.requireComplete(null, "中国"));
    PreTradeTerms.requireComplete(demand("nationwide", "buyer", "per_quote", 14, "purchase_day"), "中国");
    PreTradeTerms.requireComplete(demand("meetup", "none", "allow_keep", 7, "return_day"), "中国");
  }

  @Test
  void hardFiltersMatchTheLedgerRules() {
    var d = demand("nationwide", "carrier_free", "must_return", 14, "purchase_day");
    assertEquals("", PreTradeTerms.firstMismatch(d, offer(List.of("nationwide"), "free", "return", 10, "purchase_day")));
    assertEquals("scope_incompatible", PreTradeTerms.firstMismatch(d, offer(List.of("province"), "free", "return", 10, "purchase_day")));
    assertEquals("postage_incompatible", PreTradeTerms.firstMismatch(d, offer(List.of("nationwide"), "collect", "return", 10, "purchase_day")));
    assertEquals("tax_refund_incompatible", PreTradeTerms.firstMismatch(d, offer(List.of("nationwide"), "free", "keep", 10, "purchase_day")));
    assertEquals("lead_time_incompatible", PreTradeTerms.firstMismatch(d, offer(List.of("nationwide"), "free", "return", 15, "purchase_day")));
    assertEquals("lead_time_incompatible", PreTradeTerms.firstMismatch(d, offer(List.of("nationwide"), "free", "return", 10, "ship_day")));
  }

  @Test
  void nationwideCoversProvinceButNotTheOtherWayRound() {
    var province = demand("province", "buyer", "per_quote", 7, "ship_day");
    assertEquals("", PreTradeTerms.firstMismatch(province, offer(List.of("nationwide"), "collect", "keep", 7, "ship_day")));
    var nation = demand("nationwide", "buyer", "per_quote", 7, "ship_day");
    assertEquals("scope_incompatible", PreTradeTerms.firstMismatch(nation, offer(List.of("province"), "collect", "keep", 7, "ship_day")));
  }

  @Test
  void allowKeepOnlyMatchesKeepAndPerQuoteMatchesBoth() {
    var keep = demand("nationwide", "buyer", "allow_keep", 7, "ship_day");
    assertEquals("tax_refund_incompatible", PreTradeTerms.firstMismatch(keep, offer(List.of("nationwide"), "free", "return", 7, "ship_day")));
    var any = demand("nationwide", "buyer", "per_quote", 7, "ship_day");
    assertEquals("", PreTradeTerms.firstMismatch(any, offer(List.of("nationwide"), "free", "return", 7, "ship_day")));
    assertEquals("", PreTradeTerms.firstMismatch(any, offer(List.of("nationwide"), "free", "keep", 7, "ship_day")));
  }

  @Test
  void offerDeclarationRules() {
    var d = demand("nationwide", "negotiable", "per_quote", 14, "purchase_day");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("deliveryScopes", List.of("nationwide"));
    body.put("postageMode", "negotiable");
    body.put("taxRefundMode", "keep");
    body.put("leadTimeAnchor", "purchase_day");
    // 协商必须报上限
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.offerFromBody(body, d, 10, 1000, 0));
    // 预估不得超过上限
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.offerFromBody(body, d, 10, 5000, 3000));
    var ok = PreTradeTerms.offerFromBody(body, d, 10, 2000, 3000);
    assertEquals(3000L, ok.get("postageCapMinor"));
    // 包邮/到付时报价里的邮费必须为 0
    body.put("postageMode", "free");
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.offerFromBody(body, d, 10, 1, 0));
    // 面交没有邮费
    var meetup = demand("meetup", "none", "per_quote", 7, "return_day");
    body.put("leadTimeAnchor", "return_day");
    assertThrows(IllegalArgumentException.class, () -> PreTradeTerms.offerFromBody(body, meetup, 7, 100, 0));
  }

  @Test
  void feeFollowsTaxPlanOnGoodsBasis() {
    FinancePolicy p = new FinancePolicy();
    long goods = 10000; // ¥100.00
    assertEquals("A", PreTradeTerms.feePlan("return"));
    assertEquals("B", PreTradeTerms.feePlan("keep"));
    assertEquals(800, p.fee(goods, p.getFeeBps()) + p.taxPlanFeeDelta(goods, "A")); // 5% + 3%
    assertEquals(300, p.fee(goods, p.getFeeBps()) + p.taxPlanFeeDelta(goods, "B")); // 5% - 2%
  }

  @Test
  void noVoucherFallbackCompensatesFivePercentOfGoods() {
    var f = PreTradeTerms.noVoucherFallback(20000, PreTradeTerms.NO_VOUCHER_COMPENSATION_BPS, -200);
    assertEquals(1000L, f.get("compensationMinor"));
    assertEquals("B", f.get("feePlan"));
    assertEquals("buyer", f.get("paidTo"));
  }
}
