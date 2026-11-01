package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.account.impl.SavedCommerceService;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
class SavedCommerceTest {
 JdbcTemplate db;DomainAccessGuard guard;SavedCommerceService service;
 @BeforeEach void init(){db=mock(JdbcTemplate.class);guard=mock(DomainAccessGuard.class);service=new SavedCommerceService(db,guard);when(guard.requireActiveProfile()).thenReturn("owner");}
 SavedCommerceService.AddressInput address(){return new SavedCommerceService.AddressInput("Recipient","+8613800138000","P","C","D","Street",true);}
 @Test void noSessionCannotReadOrWritePrivateData(){when(guard.requireActiveProfile()).thenThrow(new AccessDeniedException("inactive"));assertThrows(AccessDeniedException.class,()->service.addresses(20,0));assertThrows(AccessDeniedException.class,()->service.cart("product",new SavedCommerceService.CartInput(1)));verifyNoInteractions(db);}
 @Test void anotherOwnersAddressCannotResetDefaults(){when(db.queryForList(contains("FROM t_profiles"),eq("owner"))).thenReturn(List.of(Map.of("id","owner")));assertThrows(ResponseStatusException.class,()->service.saveAddress("other-address",address()));verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void defaultAddressWritesAreSerializedByOwnerProfile(){when(db.queryForList(contains("FROM t_profiles"),eq("owner"))).thenReturn(List.of(Map.of("id","owner")));var saved=service.saveAddress(null,address());var order=inOrder(db);order.verify(db).queryForList(contains("FOR UPDATE"),eq("owner"));order.verify(db).update(contains("SET is_default=0"),eq("owner"));order.verify(db).update(startsWith("INSERT INTO t_addresses"),eq(saved.get("id")),eq("owner"),eq("Recipient"),eq("+8613800138000"),eq("P"),eq("C"),eq("D"),eq("Street"),eq(true));}
 @Test void cartCannotExceedStock(){when(db.queryForList(contains("SELECT p.stock"),eq("product"))).thenReturn(List.of(Map.of("stock",2,"owner_id","seller")));assertEquals(409,assertThrows(ResponseStatusException.class,()->service.cart("product",new SavedCommerceService.CartInput(3))).getStatusCode().value());verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void cartCannotContainOwnOrUnavailableProduct(){when(db.queryForList(contains("SELECT p.stock"),eq("own"))).thenReturn(List.of(Map.of("stock",2,"owner_id","owner")));assertThrows(ResponseStatusException.class,()->service.cart("own",new SavedCommerceService.CartInput(1)));assertThrows(ResponseStatusException.class,()->service.cart("hidden",new SavedCommerceService.CartInput(1)));verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void privateListAndRemovalRemainOwnerScoped(){service.addresses(20,40);service.deleteCart("product");service.deleteFavorite("product","target");verify(db).queryForList(contains("WHERE user_id=?"),eq("owner"),eq(20),eq(40));verify(db).update(contains("WHERE user_id=? AND product_id=?"),eq("owner"),eq("product"));verify(db).update(contains("WHERE user_id=? AND target_type=? AND target_id=?"),eq("owner"),eq("product"),eq("target"));}
 @Test void hiddenFavoriteAndUnknownTypeCannotBeInserted(){when(db.queryForObject(anyString(),eq(Boolean.class),eq("hidden"))).thenReturn(false);assertThrows(ResponseStatusException.class,()->service.favorite("demand","hidden"));assertThrows(ResponseStatusException.class,()->service.favorite("secret","hidden"));verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void cartWritesOnlyDesiredQuantityWithoutWalletMutation(){when(db.queryForList(contains("SELECT p.stock"),eq("product"))).thenReturn(List.of(Map.of("stock",2,"owner_id","seller")));service.cart("product",new SavedCommerceService.CartInput(2));verify(db).update(contains("ON DUPLICATE KEY UPDATE qty=VALUES(qty)"),anyString(),eq("owner"),eq("product"),eq(2));verify(db,never()).update(contains("t_balance_ledger"),any(Object[].class));}
 @Test void unavailableCartProductDoesNotExposeDraftNameOrPrice(){service.cart(20,0);var sql=org.mockito.ArgumentCaptor.forClass(String.class);verify(db).queryForList(sql.capture(),eq("owner"),eq(20),eq(0));assertTrue(sql.getValue().contains("THEN p.name ELSE NULL END AS name"));assertTrue(sql.getValue().contains("THEN p.price ELSE NULL END AS price"));assertTrue(sql.getValue().contains("WHERE c.user_id=?"));}

}
