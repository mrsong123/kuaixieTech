package com.kuaixietech.controller;

import com.kuaixietech.service.account.impl.AuthLoginService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {
  private final AuthLoginService auth;
  public AuthLoginController(AuthLoginService auth){this.auth=auth;}
  public record LoginBody(String channel,String contact,String password,String code){}
  @PostMapping("/login")
  public Map<String,Object> login(@RequestBody LoginBody body){
    return auth.login(body.channel(),body.contact(),body.password()==null||body.password().isBlank()?body.code():body.password());
  }
}
