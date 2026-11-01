package com.kuaixietech.controller;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

/** Active publishing options; string IDs retain the database's BIGINT precision in JavaScript. */
@RestController
@RequestMapping("/api/public/category-options")
public class PublicCategoryOptionsController {
 private final JdbcTemplate db;
 public PublicCategoryOptionsController(JdbcTemplate db){this.db=db;}
 @GetMapping public List<Map<String,Object>> list(@RequestParam(defaultValue="100") int limit,@RequestParam(defaultValue="0") int offset,@RequestParam(required=false) String search){
  if(limit<1||limit>100||offset<0||search!=null&&search.length()>100)throw new IllegalArgumentException("CATEGORY_PAGINATION_INVALID");
  String term=search==null?"":search.trim();
  return db.queryForList("SELECT CAST(c.id AS CHAR) AS id,c.name,CAST(c.parent_id AS CHAR) AS parent_id,c.level,c.sort_order,(SELECT JSON_ARRAYAGG(cc.country_code) FROM t_category_countries cc WHERE cc.category_id=c.id) AS country_codes FROM t_categories c WHERE c.active=1 AND (?='' OR LOCATE(?,c.name)>0) ORDER BY c.sort_order,c.id LIMIT ? OFFSET ?",term,term,limit,offset);
 }
}
