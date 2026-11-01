package com.kuaixietech.integration;
import java.util.*;
import org.springframework.stereotype.Service;
import com.kuaixietech.scheduler.ContactDeliveryDiagnostic;
import com.kuaixietech.scheduler.ContactDeliveryAttempt;
@Service
public class SmsOutboxDispatcher {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

 private final IntegrationConfiguration config;private final SmsOutboxStore store;private final SmsProtocols protocols;
 public SmsOutboxDispatcher(IntegrationConfiguration config,SmsOutboxStore store,SmsProtocols protocols){this.config=config;this.store=store;this.protocols=protocols;}
 private volatile ContactDeliveryDiagnostic configurationStatus=ContactDeliveryDiagnostic.state("sms","not_checked","configuration","CONTACT_NOT_CHECKED");
 public ContactDeliveryDiagnostic configurationStatus(){return configurationStatus;}
 public boolean configured(){
  try {
   if(couplerMode!=null)couplerMode.requireLiveNewCall("sms_dispatch");
   int active=0;for(String vendor:List.of("aliyun","tencent"))if(config.runtime(vendor,"sms")!=null)active++;
   if(active>1)throw new IllegalStateException("SMS_MULTIPLE_ACTIVE_PROVIDERS");
   boolean managed=active>0||config.exists("aliyun","sms")||config.exists("tencent","sms");
   configurationStatus=ContactDeliveryDiagnostic.state("sms",active>0?"ready":managed?"paused":"idle","configuration",active>0?"CONTACT_PROVIDER_CONFIGURED":managed?"CONTACT_CONFIGURATION_DISABLED_OR_UNSELECTED":"SMS_PROVIDER_NOT_CONFIGURED");
   return active>0;
  }catch(RuntimeException e){configurationStatus=ContactDeliveryDiagnostic.failure("sms","configuration",e);if(configurationStatus.paused())return false;throw e;}
 }
 public void run(){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("sms_dispatch");String vendor=null;Map<String,String> fields=null;for(String candidate:List.of("aliyun","tencent")){var settings=config.runtime(candidate,"sms");if(settings!=null){if(vendor!=null)throw new IllegalStateException("SMS_MULTIPLE_ACTIVE_PROVIDERS");vendor=candidate;fields=settings;}}if(vendor==null)throw new IllegalStateException("SMS_PROVIDER_NOT_CONFIGURED");for(int i=0;i<20;i++){var row=store.claim();if(row==null)return;String target=Objects.toString(row.get("target"),"");if(target.matches("1[3-9][0-9]{9}"))target="+86"+target;String reference="vc-"+row.get("id");try{var receipt=protocols.send(vendor,fields,target,Objects.toString(row.get("code"),""),reference);if(receipt.providerReference()==null||!receipt.providerReference().matches("[A-Za-z0-9_.:-]{1,160}"))throw new IllegalStateException("SMS_RECEIPT_UNCONFIRMED");store.result(row.get("id"),receipt.accepted()?"sent":"failed",(receipt.accepted()?"PROVIDER_ACCEPTED:":"PROVIDER_REJECTED:")+receipt.providerReference());if(!receipt.accepted())throw new ContactDeliveryAttempt("SMS_PROVIDER_REJECTED",null);}catch(ContactDeliveryAttempt e){throw e;}catch(IllegalArgumentException e){store.result(row.get("id"),"failed","SMS_INPUT_OR_REGION_REJECTED");throw new ContactDeliveryAttempt("SMS_INPUT_REJECTED",e);}catch(RuntimeException e){/* The blocked claim is retained, never resent. Surface safe operational uncertainty. */throw new ContactDeliveryAttempt("SMS_DISPATCH_UNCONFIRMED",e);}}}
}
