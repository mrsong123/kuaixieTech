package com.kuaixietech.integration;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class SmsOutboxDispatcher {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

 private final IntegrationConfiguration config;private final SmsOutboxStore store;private final SmsProtocols protocols;
 public SmsOutboxDispatcher(IntegrationConfiguration config,SmsOutboxStore store,SmsProtocols protocols){this.config=config;this.store=store;this.protocols=protocols;}
 public boolean configured(){return config.runtime("aliyun","sms")!=null||config.runtime("tencent","sms")!=null;}
 public void run(){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("sms_dispatch");String vendor=null;Map<String,String> fields=null;for(String candidate:List.of("aliyun","tencent")){var settings=config.runtime(candidate,"sms");if(settings!=null){if(vendor!=null)throw new IllegalStateException("SMS_MULTIPLE_ACTIVE_PROVIDERS");vendor=candidate;fields=settings;}}if(vendor==null)throw new IllegalStateException("SMS_PROVIDER_NOT_CONFIGURED");for(int i=0;i<20;i++){var row=store.claim();if(row==null)return;String target=Objects.toString(row.get("target"),"");if(target.matches("1[3-9][0-9]{9}"))target="+86"+target;String reference="vc-"+row.get("id");try{var receipt=protocols.send(vendor,fields,target,Objects.toString(row.get("code"),""),reference);if(receipt.providerReference()==null||!receipt.providerReference().matches("[A-Za-z0-9_.:-]{1,160}"))throw new IllegalStateException("SMS_RECEIPT_UNCONFIRMED");store.result(row.get("id"),receipt.accepted()?"sent":"failed",(receipt.accepted()?"PROVIDER_ACCEPTED:":"PROVIDER_REJECTED:")+receipt.providerReference());}catch(IllegalArgumentException e){store.result(row.get("id"),"failed","SMS_INPUT_OR_REGION_REJECTED");}catch(RuntimeException e){/* A timeout, invalid receipt or DB failure must never cause a second chargeable dispatch. */return;}}}
}
