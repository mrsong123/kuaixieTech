package com.kuaixietech.service.account.impl;
import com.kuaixietech.mapper.account.ContactCodeVerificationMapper;
import com.kuaixietech.service.account.ContactCodeVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ContactCodeVerificationServiceImpl implements ContactCodeVerificationService {
 private final ContactCodeVerificationMapper mapper;
 public ContactCodeVerificationServiceImpl(ContactCodeVerificationMapper mapper){this.mapper=mapper;}
 @Override @Transactional public boolean verifyPrelogin(String target,String channel,String code){
   if(target==null||channel==null||code==null)return false;
   // Atomic compare-and-set prevents the same code being consumed twice under concurrent requests.
   return mapper.consumeLatestValid(target,channel,code)>0;
 }
}
