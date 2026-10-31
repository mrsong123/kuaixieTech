package com.kuaixietech.mapper.communication;
import org.apache.ibatis.annotations.Mapper;
@Mapper public interface PrivacyReleaseMigrationMapper {
 int releaseExpiredBindings();
 int releaseIdleNumbers();
}
