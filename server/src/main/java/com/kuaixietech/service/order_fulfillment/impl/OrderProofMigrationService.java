package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.mapper.order_fulfillment.OrderProofMigrationMapper;
import com.kuaixietech.security.TrustedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Source: confirm_purchase(v3), confirm_ship, record_boarding_proof, record_delivery_proof, request_no_boarding_refund. */
@Service public class OrderProofMigrationService {
 private com.kuaixietech.commerce.ProductInventory inventory;
 @org.springframework.beans.factory.annotation.Autowired public void productInventory(com.kuaixietech.commerce.ProductInventory inventory){this.inventory=inventory;}
 private final OrderProofMigrationMapper m;private final AtomicMoneyService money;private final ObjectMapper json;
 public OrderProofMigrationService(OrderProofMigrationMapper m,AtomicMoneyService money,ObjectMapper json){this.m=m;this.money=money;this.json=json;}
 private static String s(Object o){return o==null?null:String.valueOf(o);}
 private static BigDecimal d(Object o){return o==null?BigDecimal.ZERO:new BigDecimal(s(o));}
 private static String trim(String s){return s==null||s.trim().isEmpty()?null:s.trim();}
 private static String id(){return UUID.randomUUID().toString();}
 private static void changed(int n){if(n!=1)throw new IllegalStateException("Concurrent transition or missing row");}
 private static boolean location(BigDecimal lat,BigDecimal lng){return lat!=null&&lng!=null&&lat.abs().compareTo(new BigDecimal("90"))<=0&&lng.abs().compareTo(new BigDecimal("180"))<=0;}
 private JsonNode tree(String raw){try{return json.readTree(raw==null?"null":raw);}catch(Exception e){throw new IllegalArgumentException("Invalid JSON",e);}}
 private String asJson(JsonNode node){try{return json.writeValueAsString(node);}catch(Exception e){throw new IllegalArgumentException("JSON encoding failed",e);}}
 private static LocalDateTime dt(Object value){if(value==null)return null;if(value instanceof java.sql.Timestamp t)return t.toLocalDateTime();if(value instanceof LocalDateTime t)return t;return LocalDateTime.parse(String.valueOf(value).replace(' ','T'));}
 private static LocalDate date(Object value){if(value instanceof java.sql.Date d)return d.toLocalDate();return LocalDate.parse(String.valueOf(value).substring(0,10));}
 private static void fresh(Map<String,Object> face){LocalDateTime at=dt(face.get("created_at"));if(at==null||at.isBefore(LocalDateTime.now().minusMinutes(15)))throw new IllegalStateException("FACE_STALE");}
 private void notice(String uid,String kind,String title,String body,String order){if(uid!=null)changed(m.addNotice(id(),uid,kind,title,body,order));}
 private boolean selectedInsuranceReady(Map<String,Object> order,String id){
  JsonNode agreed=tree(s(order.get("agreed_json")));
  return !com.kuaixietech.service.trading.support.IntentPricing.EXTERNAL_VERSION.equals(agreed.path("pricing_version").asText())||!"yes".equals(agreed.path("insurance_choice").asText())||m.activeGoodsInsurance(id)==1;
 }
 @Transactional public String confirmPurchase(String orderId,String evidence,BigDecimal lat,BigDecimal lng,String city,String address,String metadata){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);if(o==null)return "ERR:NO_ORDER";
  if(!actor.equals(s(o.get("vendor_id"))))return "ERR:NOT_VENDOR";
  if(!selectedInsuranceReady(o,orderId))return "ERR:INSURANCE_NOT_EFFECTIVE";
  if(!"locked".equals(s(o.get("status"))))return "ERR:BAD_STATUS";
  JsonNode e=tree(evidence);if(e==null||!e.isObject())return "ERR:BAD_EVIDENCE";
  for(String k:List.of("receipt","goods","storefront")){
   if(!e.path(k).isArray()||e.path(k).isEmpty())return "ERR:NO_"+k.toUpperCase(Locale.ROOT);
  }
  if("refund_to_buyer".equals(s(o.get("tax_refund_plan")))&&(!e.path("tax_refund_receipt").isArray()||e.path("tax_refund_receipt").isEmpty()))return "ERR:NO_TAX_PROOF";
  if(!location(lat,lng))return "ERR:NO_LOCATION";
  Map<String,Object> f=m.latestFace(actor,orderId,"purchase_confirm");if(f==null)return "ERR:NO_FACE";
  if(dt(f.get("created_at")).isBefore(LocalDateTime.now().minusMinutes(15)))return "ERR:FACE_STALE";
  JsonNode meta=tree(metadata==null?"{}":metadata);
  String expectedDate=LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ISO_DATE),no=s(o.get("order_no"));
  if(!expectedDate.equals(meta.path("date_str").asText())||no==null||!no.substring(Math.max(0,no.length()-4)).equals(meta.path("order_tail").asText()))return "ERR:DATE_BAR";
  String c=trim(city);ArrayNode flags=json.createArrayNode();
  if(o.get("trip_id")!=null&&c!=null){Map<String,Object> t=m.trip(s(o.get("trip_id")));
   if(t!=null){String dest=Objects.toString(t.get("destination"),"");String prefix=dest.split("[ (]",2)[0];
    if(!c.contains(prefix)&&!dest.contains(c))flags.add("city_not_match_trip");
    if("domestic".equals(s(t.get("trip_type")))){
     String origin=Objects.toString(t.get("origin_city"),"");
     if(!origin.contains(c)&&!dest.contains(c)&&!c.contains(origin)&&!c.contains(prefix))flags.add("domestic_city_mismatch");
    }
   }
  }
  ArrayNode images=json.createArrayNode();JsonNode old=tree(s(o.get("purchase_receipt_urls")));
  if(old!=null&&old.isArray())old.forEach(images::add);
  for(String k:List.of("receipt","goods","storefront"))e.path(k).forEach(images::add);
  BigDecimal rate=o.get("tax_rate")==null?new BigDecimal("0.08"):d(o.get("tax_rate"));
  BigDecimal tax=d(o.get("goods_amount")).multiply(rate).setScale(2,RoundingMode.HALF_UP);
  // PostgreSQL extracts the first city suffix from a Chinese address.
  String shippingCity=null;String shippingAddress=s(o.get("address"));if(shippingAddress!=null){java.util.regex.Matcher match=java.util.regex.Pattern.compile("^([\\p{IsHan}]{2,8}市)").matcher(shippingAddress);if(match.find())shippingCity=match.group(1);}
  changed(m.savePurchase(orderId,asJson(e),asJson(meta),asJson(images),lat,lng,c,trim(address),s(f.get("id")),asJson(flags),tax,shippingCity!=null&&c!=null&&!shippingCity.equals(c)));
  if(inventory!=null)inventory.transition(orderId,"consumed");
  changed(m.addMessage(id(),orderId,actor,"带单人提交采购确认：三件套证据（含日期条）、采购定位与口令刷脸已留证，订单进入已采购状态"+("refund_to_buyer".equals(s(o.get("tax_refund_plan")))?"；退税凭证已随单上传":"")));
  notice(s(o.get("buyer_id")),"purchased","带单人已确认采购","小票/商品图/门店照与采购定位已上传，可在订单详情查看溯源信息",orderId);return "OK";
 }
 @Transactional public String confirmShip(String orderId,String carrier,String tracking,String labelUrls,BigDecimal lat,BigDecimal lng,String city,String address){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);if(o==null)return "ERR:NO_ORDER";
  if(!actor.equals(s(o.get("vendor_id"))))return "ERR:NOT_VENDOR";
  if(!m.activeCases(orderId).isEmpty()||!m.activeAfterSales(orderId).isEmpty())return "ERR:DISPUTE_OPEN";
  if(!selectedInsuranceReady(o,orderId))return "ERR:INSURANCE_NOT_EFFECTIVE";
  if(!"purchased".equals(s(o.get("status"))))return "ERR:BAD_STATUS";
  if(trim(carrier)==null||trim(tracking)==null)return "ERR:NO_TRACKING";
  JsonNode labels=tree(labelUrls);if(labels==null||!labels.isArray()||labels.isEmpty())return "ERR:NO_LABEL";
  if(!location(lat,lng))return "ERR:NO_LOCATION";
  ArrayNode flags=json.createArrayNode();String c=trim(city);
  if(o.get("trip_id")!=null){Map<String,Object> t=m.trip(s(o.get("trip_id")));if(t!=null&&t.get("return_date")!=null&&LocalDate.now(ZoneId.of("Asia/Shanghai")).isBefore(date(t.get("return_date"))))flags.add("ship_before_return");}
  if(c!=null&&c.equals(s(o.get("procurement_city"))))flags.add("ship_city_mismatch");
  changed(m.saveShipment(orderId,carrier.trim(),tracking.trim(),asJson(labels),lat,lng,c,trim(address),asJson(flags)));
  changed(m.addMessage(id(),orderId,actor,"带单人已发货："+carrier.trim()+" "+tracking.trim()+"（面单与寄出定位已留证）"));
  notice(s(o.get("buyer_id")),"shipped","包裹已寄出",carrier.trim()+" · "+tracking.trim()+"，10 天后自动确认收货",orderId);return "OK";
 }
 @Transactional public String recordBoardingProof(String tripId,String snapshotUrl,BigDecimal lat,BigDecimal lng,String city){
  String actor=TrustedActor.currentUserId();Map<String,Object> t=m.lockTrip(tripId);if(t==null)return "ERR:NO_TRIP";
  if(!actor.equals(s(t.get("owner_id"))))return "ERR:NOT_OWNER";
  if(!"approved".equals(s(t.get("verify_status"))))return "ERR:NOT_VERIFIED";
  if(snapshotUrl==null||snapshotUrl.length()<8)return "ERR:NO_SNAPSHOT";
  ArrayNode flags=json.createArrayNode();LocalDateTime depart=dt(t.get("depart_datetime"));
  if(depart!=null&&Math.abs(Duration.between(depart,LocalDateTime.now()).toMinutes())>360)flags.add("boarding_out_of_window");
  changed(m.saveBoarding(tripId,snapshotUrl,lat,lng,trim(city),asJson(flags)));return "OK";
 }
 @Transactional public String recordDeliveryProof(String orderId,String faceId,BigDecimal lat,BigDecimal lng,String city){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);if(o==null)return "ERR:NO_ORDER";
  if(!actor.equals(s(o.get("buyer_id"))))return "ERR:NOT_BUYER";
  if(!"shipped".equals(s(o.get("status"))))return "ERR:BAD_STATUS";
  if(faceId==null)return "ERR:NO_FACE";
  Map<String,Object> face=m.exactFace(faceId,actor,orderId);if(face==null)return "ERR:FACE_MISMATCH";
  if(dt(face.get("created_at")).isBefore(LocalDateTime.now().minusMinutes(15)))return "ERR:FACE_STALE";
  if(!location(lat,lng))return "ERR:NO_LOCATION";
  changed(m.saveDelivery(orderId,faceId,lat,lng,trim(city)));
  changed(m.addMessage(id(),orderId,actor,"买家完成收货核验：本人刷脸与收货定位已留证"));return "OK";
 }
 @Transactional public String requestNoBoardingRefund(String orderId){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);if(o==null)return "ERR:NO_ORDER";
  if(!actor.equals(s(o.get("buyer_id"))))return "ERR:NOT_BUYER";
  if(!m.activeCases(orderId).isEmpty()||!m.activeAfterSales(orderId).isEmpty())return "ERR:DISPUTE_OPEN";
  if(!Set.of("requesting","negotiating","vendor_deposit_pending","locked").contains(s(o.get("status"))))return "ERR:BAD_STATUS";
  if(o.get("trip_id")==null)return "ERR:NO_TRIP";
  Map<String,Object> trip=m.trip(s(o.get("trip_id")));if(trip==null)return "ERR:NO_TRIP";
  if(trip.get("boarding_at")!=null)return "ERR:BOARDING_DONE";
  LocalDateTime depart=dt(trip.get("depart_datetime"));if(depart==null||LocalDateTime.now().isBefore(depart.plusHours(6)))return "ERR:WINDOW_OPEN";
  if("frozen".equals(s(o.get("buyer_deposit_status"))))money.addBalance(actor,d(o.get("buyer_deposit")),"refund",orderId,"未登机留证无责退款");
  String vendor=s(o.get("vendor_id"));if(vendor!=null&&"frozen".equals(s(o.get("vendor_deposit_status"))))money.addBalance(vendor,d(o.get("vendor_deposit")),"refund",orderId,"未登机留证保证金退回");
  changed(m.cancelNoBoarding(orderId));if(inventory!=null)inventory.transition(orderId,"released");m.refundDepositRows(orderId);
  notice(actor,"order","订单已无责退回","订单 "+o.get("order_no")+" 因带单人未在航班窗口内完成登机留证，已退回你的冻结资金。",orderId);
  notice(vendor,"order","订单因未登机留证被无责取消","订单 "+o.get("order_no")+" 因未完成登机留证被平台无责关闭；冻结保证金已退回。",orderId);
  return "OK";
 }
}
