package com.kuaixietech.controller;

import com.kuaixietech.service.account.impl.AuthLoginService;
import com.kuaixietech.service.account.impl.AuthRecoveryService;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {
  private final AuthLoginService auth;
  private final AuthRecoveryService recovery;
  public AuthLoginController(AuthLoginService auth,AuthRecoveryService recovery){this.auth=auth;this.recovery=recovery;}
  public record LoginBody(String channel,String contact,String password,String code,String countryCode){}
  public record WechatBody(String code){}
  public record SendCodeBody(String channel,String contact,String countryCode,String purpose,String captchaId,String captchaCode){}
  public record ResetBody(String channel,String contact,String countryCode,String code,String newPassword){}
  public record RegisterBody(String channel,String contact,String countryCode,String password,String code,String role,String inviteCode,String enterpriseName,String businessLicenseNo){}
  public record HelpBody(String channel,String contact,String countryCode,String captchaId,String captchaCode){}

  @PostMapping("/login")
  public Map<String,Object> login(@RequestBody LoginBody body){
    return auth.login(body.channel(),body.contact(),body.password()==null||body.password().isBlank()?body.code():body.password(),body.countryCode());
  }
  @PostMapping("/wechat")
  public Map<String,Object> wechat(@RequestBody WechatBody body){
    return auth.wechat(body.code());
  }
  @GetMapping("/captcha")
  public Map<String,String> captcha(){return recovery.captcha();}
  @PostMapping("/send-code")
  public ResponseEntity<Map<String,Object>> sendCode(@RequestBody SendCodeBody body){
    return run(()->recovery.sendCode(body.channel(),body.contact(),body.countryCode(),body.purpose(),body.captchaId(),body.captchaCode()));
  }
  @PostMapping("/register")
  public ResponseEntity<Map<String,Object>> register(@RequestBody RegisterBody body){
    return run(()->recovery.register(body.channel(),body.contact(),body.countryCode(),body.password(),body.code(),body.role(),body.inviteCode(),body.enterpriseName(),body.businessLicenseNo()));
  }
  @PostMapping("/reset-password")
  public ResponseEntity<Map<String,Object>> reset(@RequestBody ResetBody body){
    return run(()->recovery.resetPassword(body.channel(),body.contact(),body.countryCode(),body.code(),body.newPassword()));
  }
  @PostMapping("/recovery-help")
  public ResponseEntity<Map<String,Object>> help(@RequestBody HelpBody body){
    return run(()->recovery.recoveryHelp(body.channel(),body.contact(),body.countryCode(),body.captchaId(),body.captchaCode()));
  }

  private ResponseEntity<Map<String,Object>> run(Supplier<Map<String,Object>> action){
    try{
      return ResponseEntity.ok(action.get());
    }catch(ResponseStatusException ex){
      String reason=ex.getReason()==null?"AUTH_FAILED":ex.getReason();
      return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",reason));
    }
  }
}
