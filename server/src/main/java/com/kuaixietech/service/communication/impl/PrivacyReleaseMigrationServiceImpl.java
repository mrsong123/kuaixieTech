package com.kuaixietech.service.communication.impl;
import com.kuaixietech.mapper.communication.PrivacyReleaseMigrationMapper;
import com.kuaixietech.service.communication.PrivacyReleaseMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class PrivacyReleaseMigrationServiceImpl implements PrivacyReleaseMigrationService {
 private final PrivacyReleaseMigrationMapper mapper;
 public PrivacyReleaseMigrationServiceImpl(PrivacyReleaseMigrationMapper mapper){this.mapper=mapper;}
 @Override @Transactional public String releaseExpired(){int count=mapper.releaseExpiredBindings();mapper.releaseIdleNumbers();return "OK:RELEASED:"+count;}
}
