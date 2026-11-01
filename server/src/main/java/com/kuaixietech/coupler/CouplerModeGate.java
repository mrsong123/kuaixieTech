package com.kuaixietech.coupler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
/** New legacy external effects require LIVE. Historical receipt/query paths do not consult this gate. */
@Service
public class CouplerModeGate {
 private final CouplerService service;
 public CouplerModeGate(CouplerService service){this.service=service;}
 public boolean liveNewActionsAllowed(){return service.current().mode()==CouplerModels.Mode.LIVE;}
 public void requireLiveNewCall(String operation){if(!liveNewActionsAllowed())throw new ResponseStatusException(HttpStatus.CONFLICT,"COUPLER_LIVE_MODE_REQUIRED");String category=switch(operation){case "private_upload"->"OSS";case "wechat_login"->"MINIAPP";case "sms_dispatch"->"SMS";case "email_dispatch"->"EMAIL";default->null;};if(category!=null)service.requireLegacyFeature(category);}
 public void requireLiveNewAction(String operation){requireLiveNewCall(operation);}
}
