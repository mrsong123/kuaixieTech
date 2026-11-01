package com.kuaixietech.mapper.catalog;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PublicCatalogMapper {
  List<Map<String,Object>> categories(@Param("limit") int limit, @Param("countryCode") String countryCode);
  Map<String,Object> product(@Param("id") String id);
  List<Map<String,Object>> products(@Param("limit") int limit);
  List<Map<String,Object>> trips(@Param("limit") int limit, @Param("sort") String sort);
  List<Map<String,Object>> maskedProfiles(@Param("ids") List<String> ids);
}
