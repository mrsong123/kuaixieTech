package com.kuaixietech.storage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class UploadBindingSafetyTest {
 final String id="123e4567-e89b-42d3-a456-426614174000",owner="223e4567-e89b-42d3-a456-426614174000";
 Map<String,Object> image(String user,String kind){return Map.of("id",id,"user_id",user,"purpose","product_image","kind",kind,"content_type","image/png","sha256","a".repeat(64),"byte_size",40);}
 @Test void privateImageReferenceMustBeCanonical(){assertEquals(id,UploadBindings.imageId("/public/media/"+id));for(String value:List.of("https://host/public/media/"+id,"/public/media/"+id+"?token=secret","/public/media/../"+id))assertNull(UploadBindings.imageId(value));}
 @Test void foreignOrDeletingImageCannotBecomeProductOrOrderEvidence(){for(var row:List.of(image("foreign","image"),image(owner,"deleting"))){var db=mock(JdbcTemplate.class);when(db.queryForList(anyString(),any(Object[].class))).thenAnswer(c->c.<String>getArgument(0).contains("FOR UPDATE")?List.of(row):List.of());var bindings=new UploadBindings(db);assertThrows(ResponseStatusException.class,()->bindings.replaceProduct("product",owner,List.of("/public/media/"+id)));assertThrows(ResponseStatusException.class,()->bindings.bindOrder("order",owner,List.of("/public/media/"+id)));verify(db,never()).update(anyString(),any(Object[].class));}}
 @Test void boundImageCannotBeClaimedForDeletion(){var db=mock(JdbcTemplate.class);var row=new HashMap<String,Object>(image(owner,"image"));row.put("storage_key","immutable");when(db.queryForList(anyString(),any(Object[].class))).thenAnswer(c->c.<String>getArgument(0).contains("FROM t_community_uploads")?List.of(row):List.of(Map.of("upload_id",id)));assertThrows(ResponseStatusException.class,()->new UploadLifecycle(db).begin(id,owner));verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void deletionClaimIsDurableBeforeRemoteWorkAndCanonicalAcrossCase(){var db=mock(JdbcTemplate.class);var row=new HashMap<String,Object>(image(owner,"image"));row.put("storage_key","immutable");when(db.queryForList(anyString(),any(Object[].class))).thenAnswer(c->c.<String>getArgument(0).contains("FROM t_community_uploads")?List.of(row):List.of());when(db.update(anyString(),any(Object[].class))).thenReturn(1);var lifecycle=new UploadLifecycle(db);var result=lifecycle.begin(id.toUpperCase(Locale.ROOT),owner);assertEquals(id,result.uploadId());assertEquals("deleting",result.status());verify(db).update(startsWith("INSERT INTO t_upload_deletions"),eq(result.id()),eq(id),eq(owner));verify(db).update(startsWith("UPDATE t_community_uploads SET kind='deleting'"),eq(id),eq("image"));}
}
