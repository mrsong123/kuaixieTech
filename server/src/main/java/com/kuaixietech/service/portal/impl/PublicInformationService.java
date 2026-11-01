package com.kuaixietech.service.portal.impl;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Public projections deliberately exclude internal creators, order IDs and reviewer identities. */
@Service
public class PublicInformationService {
 private final JdbcTemplate db;
 public PublicInformationService(JdbcTemplate db){this.db=db;}
 private int bounded(int limit){if(limit<1||limit>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"limit must be 1..100");return limit;}
 public List<Map<String,Object>> announcements(int limit){
  var rows=db.queryForList("SELECT id,title,body,kind,pinned,created_at FROM t_announcements WHERE active=1 ORDER BY pinned DESC,created_at DESC,id DESC LIMIT ?",bounded(limit));
  for(var row:rows){Object pinned=row.get("pinned");row.put("pinned",Boolean.TRUE.equals(pinned)||(pinned instanceof Number n&&n.intValue()!=0));}
  return rows;
 }
 public Map<String,Object> legal(String key){
  if(key==null||!key.matches("[a-z][a-z0-9_-]{0,63}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid document key");
  var rows=db.queryForList("SELECT id,doc_key,title,body_md,version,updated_at FROM t_legal_docs WHERE doc_key=?",key);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"document not published");
  return rows.getFirst();
 }
 public List<Map<String,Object>> reviews(String target,int limit){
  bounded(limit);
  if(target==null||!target.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid review target");
  return db.queryForList("SELECT r.id,r.target_id,r.stars,r.content,r.created_at FROM t_reviews r JOIN t_profiles target ON target.id=r.target_id JOIN t_profiles author ON author.id=r.reviewer_id WHERE r.target_id=? AND r.hidden=0 AND target.deleted_at IS NULL AND author.deleted_at IS NULL AND (target.banned_until IS NULL OR target.banned_until<=CURRENT_TIMESTAMP(3)) AND (author.banned_until IS NULL OR author.banned_until<=CURRENT_TIMESTAMP(3)) ORDER BY r.created_at DESC,r.id DESC LIMIT ?",target,limit);
 }
}
