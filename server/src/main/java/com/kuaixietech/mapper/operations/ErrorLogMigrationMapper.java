package com.kuaixietech.mapper.operations;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface ErrorLogMigrationMapper {
 Integer isAdmin(@Param("admin") String admin);
 int resolve(@Param("id") long id);
 int log(@Param("id") String id,@Param("admin") String admin,@Param("target") String target);
}
