package com.kuaixietech.mapper.internationalization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface LocaleMigrationMapper {
 Integer isAdmin(@Param("admin") String admin);
 int upsert(@Param("code") String code,@Param("name") String name,@Param("label") String label,@Param("sort") int sort,@Param("fallback") String fallback);
 Integer exists(@Param("code") String code);
 int toggle(@Param("code") String code,@Param("active") boolean active);
 int delete(@Param("code") String code);
 int log(@Param("id") String id,@Param("admin") String admin,@Param("action") String action,@Param("target") String target,@Param("detail") String detail);
}
