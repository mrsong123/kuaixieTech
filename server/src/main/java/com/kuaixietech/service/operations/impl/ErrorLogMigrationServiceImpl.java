package com.kuaixietech.service.operations.impl;
import com.kuaixietech.service.operations.ErrorLogMigrationService;
import com.kuaixietech.mapper.operations.ErrorLogMigrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service public class ErrorLogMigrationServiceImpl implements ErrorLogMigrationService {
 private final ErrorLogMigrationMapper mapper;
 public ErrorLogMigrationServiceImpl(ErrorLogMigrationMapper mapper){this.mapper=mapper;}
 @Override @Transactional public String resolve(String actor,long logId){
  if(actor==null)return "ERR:NO_AUTH";
  if(mapper.isAdmin(actor)==0)return "ERR:NOT_ADMIN";
  if(mapper.resolve(logId)==0)return "ERR:NOT_FOUND";
  mapper.log(UUID.randomUUID().toString(),actor,Long.toString(logId));return "OK";
 }
}
