package com.kuaixietech.storage;
import java.util.*;
import java.util.regex.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
/** Mutations join the owning product/order transaction. Uploads are always locked in UUID order. */
@Service
public class UploadBindings {
 private final JdbcTemplate db;
 private static final Pattern IMAGE=Pattern.compile("/public/media/([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})");
 public UploadBindings(JdbcTemplate db){this.db=db;}
 public static String imageId(String path){var match=IMAGE.matcher(path);return match.matches()?match.group(1):null;}
 public static List<String> ids(List<String> paths){var result=new TreeSet<String>();for(String path:paths){String id=imageId(path);if(id!=null)result.add(id);}return List.copyOf(result);}
 private Map<String,Object> lock(String id){var rows=db.queryForList("SELECT id,user_id,purpose,kind,content_type,sha256,byte_size FROM t_community_uploads WHERE id=? FOR UPDATE",id);if(rows.isEmpty())throw invalid();return rows.getFirst();}
 private ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.CONFLICT,"PRODUCT_IMAGE_UNAVAILABLE");}
 private void usable(Map<String,Object> row,String owner){if(!owner.equalsIgnoreCase(Objects.toString(row.get("user_id"),""))||!"product_image".equals(row.get("purpose"))||!"image".equals(row.get("kind"))||!Set.of("image/jpeg","image/png").contains(row.get("content_type"))||!Objects.toString(row.get("sha256"),"").matches("[a-f0-9]{64}")||!(row.get("byte_size") instanceof Number size)||size.longValue()<1||size.longValue()>PrivateUploads.LIMIT)throw invalid();}
 @Transactional(propagation=Propagation.MANDATORY) public void replaceProduct(String product,String owner,List<String> paths){var next=new TreeSet<>(ids(paths));var old=new TreeSet<String>();for(var row:db.queryForList("SELECT upload_id FROM t_upload_bindings WHERE domain_type='product' AND resource_id=?",product))old.add(row.get("upload_id").toString());var all=new TreeSet<>(old);all.addAll(next);for(String id:all){var row=lock(id);if(next.contains(id))usable(row,owner);}
  for(String id:next){db.update("INSERT IGNORE INTO t_upload_bindings(upload_id,domain_type,resource_id,created_by) VALUES(?,'product',?,?)",id,product,owner);db.update("UPDATE t_community_uploads SET consumed_at=COALESCE(consumed_at,CURRENT_TIMESTAMP(3)) WHERE id=? AND user_id=? AND kind='image'",id,owner);}
  for(String id:old)if(!next.contains(id)){db.update("DELETE FROM t_upload_bindings WHERE upload_id=? AND domain_type='product' AND resource_id=?",id,product);if(db.queryForList("SELECT upload_id FROM t_upload_bindings WHERE upload_id=? LIMIT 1",id).isEmpty())db.update("UPDATE t_community_uploads SET consumed_at=NULL WHERE id=? AND purpose='product_image' AND kind='image'",id);}
 }
 @Transactional(propagation=Propagation.MANDATORY) public void bindOrder(String order,String vendor,List<String> paths){for(String id:ids(paths)){usable(lock(id),vendor);db.update("INSERT IGNORE INTO t_upload_bindings(upload_id,domain_type,resource_id,created_by) VALUES(?,'order',?,?)",id,order,vendor);db.update("UPDATE t_community_uploads SET consumed_at=COALESCE(consumed_at,CURRENT_TIMESTAMP(3)) WHERE id=? AND kind='image'",id);}}
 public boolean orderParticipant(String upload,String actor){return !db.queryForList("SELECT b.upload_id FROM t_upload_bindings b JOIN t_orders o ON o.id=b.resource_id WHERE b.upload_id=? AND b.domain_type='order' AND (o.buyer_id=? OR o.vendor_id=?) LIMIT 1",upload,actor,actor).isEmpty();}
}
