package com.kuaixietech.service.account.impl;
import com.kuaixietech.security.DomainAccessGuard;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SavedCommerceService {
 public record AddressInput(@NotBlank @Size(max=64) String contactName,@NotBlank @Pattern(regexp="[+0-9() -]{5,32}") String contactPhone,@Size(max=64) String province,@Size(max=64) String city,@Size(max=64) String district,@NotBlank @Size(max=512) String detail,boolean isDefault){}
 public record CartInput(@Min(1) @Max(10000) int quantity){}
 private final JdbcTemplate db;private final DomainAccessGuard guard;
 private static final String PRODUCT="p.status='on' AND p.stock>0 AND s.carrier_verify_status='approved' AND NOT EXISTS(SELECT 1 FROM t_store_products sp JOIN t_stores st ON st.id=sp.store_id WHERE sp.product_id=p.id AND (st.owner_id<>p.owner_id OR st.status<>'active')) AND s.deleted_at IS NULL AND (s.banned_until IS NULL OR s.banned_until<=CURRENT_TIMESTAMP(3))";
 public SavedCommerceService(JdbcTemplate db,DomainAccessGuard guard){this.db=db;this.guard=guard;}
 private void page(int limit,int offset){if(limit<1||limit>100||offset<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid pagination");}
 private void type(String type){if(!Set.of("product","demand").contains(type))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid favorite type");}
 private void absent(){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"item unavailable");}
 private String lockOwner(){String user=guard.requireActiveProfile();if(db.queryForList("SELECT id FROM t_profiles WHERE id=? FOR UPDATE",user).isEmpty())absent();return user;}
 public List<Map<String,Object>> addresses(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.queryForList("SELECT id,contact_name,contact_phone,province,city,district,detail,is_default,created_at,updated_at FROM t_addresses WHERE user_id=? ORDER BY is_default DESC,created_at DESC,id DESC LIMIT ? OFFSET ?",user,limit,offset);}
 @Transactional public Map<String,String> saveAddress(String id,AddressInput input){
  String user=lockOwner();boolean create=id==null;if(create)id=UUID.randomUUID().toString();
  else if(db.queryForList("SELECT id FROM t_addresses WHERE id=? AND user_id=? FOR UPDATE",id,user).isEmpty())absent();
  if(input.isDefault())db.update("UPDATE t_addresses SET is_default=0 WHERE user_id=? AND is_default=1",user);
  if(create)db.update("INSERT INTO t_addresses(id,user_id,contact_name,contact_phone,province,city,district,detail,is_default) VALUES(?,?,?,?,?,?,?,?,?)",id,user,input.contactName().trim(),input.contactPhone().trim(),input.province(),input.city(),input.district(),input.detail().trim(),input.isDefault());
  else db.update("UPDATE t_addresses SET contact_name=?,contact_phone=?,province=?,city=?,district=?,detail=?,is_default=? WHERE id=? AND user_id=?",input.contactName().trim(),input.contactPhone().trim(),input.province(),input.city(),input.district(),input.detail().trim(),input.isDefault(),id,user);
  return Map.of("id",id);
 }
 @Transactional public void deleteAddress(String id){String user=lockOwner();if(db.update("DELETE FROM t_addresses WHERE id=? AND user_id=?",id,user)!=1)absent();}
 public List<Map<String,Object>> cart(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.queryForList("SELECT c.id,c.product_id,c.qty,c.created_at,CASE WHEN "+PRODUCT+" THEN p.name ELSE NULL END AS name,CASE WHEN "+PRODUCT+" THEN p.spec ELSE NULL END AS spec,CASE WHEN "+PRODUCT+" THEN p.price ELSE NULL END AS price,CASE WHEN "+PRODUCT+" THEN p.stock ELSE NULL END AS stock,("+PRODUCT+") AS available FROM t_cart_items c JOIN t_products p ON p.id=c.product_id JOIN t_profiles s ON s.id=p.owner_id WHERE c.user_id=? ORDER BY c.created_at DESC,c.id DESC LIMIT ? OFFSET ?",user,limit,offset);}
 @Transactional public void cart(String product,CartInput input){
  String user=guard.requireActiveProfile();
  var rows=db.queryForList("SELECT p.stock,p.owner_id FROM t_products p JOIN t_profiles s ON s.id=p.owner_id WHERE p.id=? AND "+PRODUCT+" FOR UPDATE",product);
  if(rows.isEmpty())absent();
  if(user.equals(rows.getFirst().get("owner_id")))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"cannot add own product");
  if(input.quantity()>((Number)rows.getFirst().get("stock")).intValue())throw new ResponseStatusException(HttpStatus.CONFLICT,"insufficient stock");
  db.update("INSERT INTO t_cart_items(id,user_id,product_id,qty) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE qty=VALUES(qty)",UUID.randomUUID().toString(),user,product,input.quantity());
 }
 @Transactional public void deleteCart(String product){String user=guard.requireActiveProfile();db.update("DELETE FROM t_cart_items WHERE user_id=? AND product_id=?",user,product);}
 public List<Map<String,Object>> favorites(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.queryForList("SELECT f.id,f.target_type,f.target_id,f.created_at,CASE WHEN f.target_type='product' THEN p.name ELSE d.title END AS title FROM t_favorites f LEFT JOIN (t_products p JOIN t_profiles s ON s.id=p.owner_id) ON f.target_type='product' AND p.id=f.target_id AND "+PRODUCT+" LEFT JOIN (t_demands d JOIN t_profiles a ON a.id=d.publisher_id) ON f.target_type='demand' AND d.id=f.target_id AND d.visibility='public' AND d.status='open' AND a.deleted_at IS NULL AND (a.banned_until IS NULL OR a.banned_until<=CURRENT_TIMESTAMP(3)) AND (d.deadline IS NULL OR d.deadline>CURRENT_TIMESTAMP(3)) WHERE f.user_id=? ORDER BY f.created_at DESC,f.id DESC LIMIT ? OFFSET ?",user,limit,offset);}
 @Transactional public void favorite(String type,String target){
  String user=guard.requireActiveProfile();type(type);
  Boolean visible=type.equals("product")?db.queryForObject("SELECT EXISTS(SELECT 1 FROM t_products p JOIN t_profiles s ON s.id=p.owner_id WHERE p.id=? AND "+PRODUCT+")",Boolean.class,target):db.queryForObject("SELECT EXISTS(SELECT 1 FROM t_demands d JOIN t_profiles s ON s.id=d.publisher_id WHERE d.id=? AND d.visibility='public' AND d.status='open' AND s.deleted_at IS NULL AND (s.banned_until IS NULL OR s.banned_until<=CURRENT_TIMESTAMP(3)) AND (d.deadline IS NULL OR d.deadline>CURRENT_TIMESTAMP(3)))",Boolean.class,target);
  if(!Boolean.TRUE.equals(visible))absent();
  db.update("INSERT INTO t_favorites(id,user_id,target_type,target_id) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE id=id",UUID.randomUUID().toString(),user,type,target);
 }
 @Transactional public void deleteFavorite(String type,String target){String user=guard.requireActiveProfile();type(type);db.update("DELETE FROM t_favorites WHERE user_id=? AND target_type=? AND target_id=?",user,type,target);}
}
