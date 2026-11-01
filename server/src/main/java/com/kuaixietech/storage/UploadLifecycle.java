package com.kuaixietech.storage;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
/** Claim deletion before any remote call. The immutable object key can be safely retried after uncertainty. */
@Service
public class UploadLifecycle {
 private final JdbcTemplate db;
 public UploadLifecycle(JdbcTemplate db){this.db=db;}
 public record Deletion(String id,String uploadId,String storageKey,String status){}
 @Transactional(isolation=Isolation.READ_COMMITTED) public Deletion begin(String upload,String user){var rows=db.queryForList("SELECT id,user_id,storage_key,kind,purpose,consumed_at FROM t_community_uploads WHERE id=? FOR UPDATE",upload);if(rows.isEmpty()||!user.equalsIgnoreCase(Objects.toString(rows.getFirst().get("user_id"),"")))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"UPLOAD_UNAVAILABLE");var row=rows.getFirst();String id=UUID.nameUUIDFromBytes(("delete-upload:"+upload).getBytes(StandardCharsets.UTF_8)).toString(),kind=row.get("kind").toString();if(kind.equals("deleted"))return new Deletion(id,upload,row.get("storage_key").toString(),"deleted");if(kind.equals("deleting"))return new Deletion(id,upload,row.get("storage_key").toString(),"deleting");if(!Set.of("image","document").contains(kind))throw new ResponseStatusException(HttpStatus.CONFLICT,"UPLOAD_NOT_DELETABLE");
  if(row.get("consumed_at")!=null||!db.queryForList("SELECT upload_id FROM t_upload_bindings WHERE upload_id=? LIMIT 1",upload).isEmpty()||!db.queryForList("SELECT id FROM t_products WHERE JSON_CONTAINS(image_urls,JSON_QUOTE(?)) LIMIT 1","/public/media/"+upload).isEmpty()||!db.queryForList("SELECT id FROM t_orders WHERE JSON_CONTAINS(ref_image_urls,JSON_QUOTE(?)) LIMIT 1","/public/media/"+upload).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"UPLOAD_IN_USE");
  if(db.update("INSERT INTO t_upload_deletions(id,upload_id,user_id,status) VALUES(?,?,?,'deleting')",id,upload,user)!=1||db.update("UPDATE t_community_uploads SET kind='deleting' WHERE id=? AND kind=?",upload,kind)!=1)throw new IllegalStateException("UPLOAD_DELETE_CLAIM_FAILED");return new Deletion(id,upload,row.get("storage_key").toString(),"deleting");
 }
 @Transactional public void complete(Deletion d){var rows=db.queryForList("SELECT kind FROM t_community_uploads WHERE id=? FOR UPDATE",d.uploadId());if(rows.isEmpty())throw new IllegalStateException("UPLOAD_DELETE_UNCONFIRMED");if("deleted".equals(rows.getFirst().get("kind")))return;if(!"deleting".equals(rows.getFirst().get("kind")))throw new IllegalStateException("UPLOAD_DELETE_STATE_CHANGED");if(db.update("UPDATE t_community_uploads SET kind='deleted' WHERE id=? AND kind='deleting'",d.uploadId())!=1||db.update("UPDATE t_upload_deletions SET status='deleted',last_error_code=NULL WHERE id=? AND status='deleting'",d.id())!=1)throw new IllegalStateException("UPLOAD_DELETE_UNCONFIRMED");}
 public void unconfirmed(Deletion d){db.update("UPDATE t_upload_deletions SET last_error_code='STORAGE_DELETE_UNCONFIRMED' WHERE id=? AND status='deleting'",d.id());}
 public List<Map<String,Object>> mine(String user){return db.queryForList("SELECT id,upload_id,status,last_error_code,created_at,updated_at FROM t_upload_deletions WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT 100",user);}
}
