package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.catalog.support.ProductPublishingService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
class ProductPublishingTest {
 JdbcTemplate db;DomainAccessGuard guard;ProductPublishingService service;
 @BeforeEach void init(){db=mock(JdbcTemplate.class);guard=mock(DomainAccessGuard.class);service=new ProductPublishingService(db,guard,new ObjectMapper(),"uploads.example.com");when(guard.requireVerifiedCarrier()).thenReturn("seller");when(guard.requireActiveProfile()).thenReturn("seller");}
 ProductPublishingService.Input input(List<String> images){return new ProductPublishingService.Input("Camera","new","JP",new BigDecimal("100.01"),2,null,images,"sealed");}
 @Test void unverifiedCannotCreate(){when(guard.requireVerifiedCarrier()).thenThrow(new AccessDeniedException("carrier required"));assertThrows(AccessDeniedException.class,()->service.create(input(List.of())));verifyNoInteractions(db);}
 @Test void newProductIsOffAndExactCnyPriceIsPreserved(){var result=service.create(input(List.of("https://uploads.example.com/a.jpg")));assertEquals("off",result.get("status"));verify(db).update(contains("'off'"),eq(result.get("id")),eq("seller"),isNull(),eq("Camera"),eq("new"),eq("JP"),eq(new BigDecimal("100.01")),eq(2),eq("[\"https://uploads.example.com/a.jpg\"]"),eq("sealed"));}
 @Test void untrustedImageHostAndCustomPortCannotWrite(){for(String image:List.of("https://other.example.com/a.jpg","https://uploads.example.com:8443/a.jpg","http://uploads.example.com/a.jpg"))assertThrows(ResponseStatusException.class,()->service.create(input(List.of(image))));verifyNoInteractions(db);}
 @Test void editingAnotherOwnersProductCannotWrite(){when(db.queryForList(anyString(),eq("other"),eq("seller"))).thenReturn(List.of());var error=assertThrows(ResponseStatusException.class,()->service.update("other",input(List.of())));assertEquals(404,error.getStatusCode().value());verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void editingForcesRelistingAndCannotChangeOwnership(){when(db.queryForList(anyString(),eq("own"),eq("seller"))).thenReturn(List.of(Map.of("id","own")));assertEquals("off",service.update("own",input(List.of())).get("status"));verify(db).update(contains("status='off' WHERE id=? AND owner_id=?"),isNull(),eq("Camera"),eq("new"),eq("JP"),eq(new BigDecimal("100.01")),eq(2),eq("[]"),eq("sealed"),eq("own"),eq("seller"));}
 @Test void zeroStockCannotBeListed(){when(db.queryForList(anyString(),eq("own"),eq("seller"))).thenReturn(List.of(Map.of("stock",0)));assertThrows(ResponseStatusException.class,()->service.state("own",new ProductPublishingService.State("on")));verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void revokedCarrierMayStillUnlistOwnProduct(){when(db.queryForList(anyString(),eq("own"),eq("seller"))).thenReturn(List.of(Map.of("stock",2)));assertEquals("off",service.state("own",new ProductPublishingService.State("off")).get("status"));verify(guard,never()).requireVerifiedCarrier();verify(db).update(anyString(),eq("off"),eq("own"),eq("seller"));}
 @Test void heldInventoryPreventsChangingSkuIdentity(){
  when(db.queryForList(contains("FROM t_products WHERE id=? AND owner_id=? FOR UPDATE"),eq("own"),eq("seller"))).thenReturn(List.of(Map.of("id","own","stock",2,"name","Camera","spec","new","region","JP","note","sealed","image_urls","[]")));
  when(db.queryForList(contains("stock_state='held'"),eq("own"))).thenReturn(List.of(Map.of("order_id","order")));
  var changed=new ProductPublishingService.Input("Another item","new","JP",new BigDecimal("100.01"),2,null,List.of(),"sealed",2);
  var error=assertThrows(ResponseStatusException.class,()->service.update("own",changed));assertEquals("PRODUCT_IDENTITY_HELD",error.getReason());verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void heldInventoryStillAllowsConfirmedRestockAndFuturePriceWithoutChangingSku(){
  when(db.queryForList(contains("FROM t_products WHERE id=? AND owner_id=? FOR UPDATE"),eq("own"),eq("seller"))).thenReturn(List.of(Map.of("id","own","stock",2,"name","Camera","spec","new","region","JP","note","sealed","image_urls","[  ]")));
  when(db.queryForList(contains("stock_state='held'"),eq("own"))).thenReturn(List.of(Map.of("order_id","order")));
  when(db.queryForList(contains("WHERE product_id=? LIMIT 1"),eq("own"))).thenReturn(List.of(Map.of("order_id","order")));
  assertEquals("off",service.update("own",new ProductPublishingService.Input("Camera","new","JP",new BigDecimal("120.00"),5,null,List.of(),"sealed",2)).get("status"));
  verify(db).update(contains("status='off'"),isNull(),eq("Camera"),eq("new"),eq("JP"),eq(new BigDecimal("120.00")),eq(5),eq("[]"),eq("sealed"),eq("own"),eq("seller"));
 }
 @Test void staleAvailableStockCannotOverwriteProductReservations(){
  when(db.queryForList(contains("FROM t_products WHERE id=? AND owner_id=? FOR UPDATE"),eq("own"),eq("seller"))).thenReturn(List.of(Map.of("id","own","stock",1)));
  when(db.queryForList(contains("WHERE product_id=? LIMIT 1"),eq("own"))).thenReturn(List.of(Map.of("order_id","order")));
  var error=assertThrows(ResponseStatusException.class,()->service.update("own",new ProductPublishingService.Input("Camera","new","JP",new BigDecimal("100.01"),2,null,List.of(),"sealed",2)));assertEquals("PRODUCT_STOCK_CHANGED_REFRESH",error.getReason());verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void paginationChecksAndOwnerFilter(){assertThrows(ResponseStatusException.class,()->service.mine(101,0));assertThrows(ResponseStatusException.class,()->service.mine(20,-1));service.mine(20,40);verify(db).queryForList(contains("WHERE owner_id=?"),eq("seller"),eq(20),eq(40));}
 @Test void canonicalImageUsesOwnershipBindingAndCannotBypassThroughAbsoluteHost(){String image="/public/media/123e4567-e89b-42d3-a456-426614174000";var bindings=mock(com.kuaixietech.storage.UploadBindings.class);service.uploadBindings(bindings);var result=service.create(input(List.of(image)));verify(bindings).replaceProduct(result.get("id"),"seller",List.of(image));assertThrows(ResponseStatusException.class,()->service.create(input(List.of("https://uploads.example.com"+image))));}
}
