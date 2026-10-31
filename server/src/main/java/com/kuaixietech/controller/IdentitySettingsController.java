package com.kuaixietech.controller;

import com.kuaixietech.service.account.IdentitySettingsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** All routes require an authenticated, cryptographically verified JWT subject. */
@RestController
@RequestMapping("/api/rpc/identity")
public class IdentitySettingsController {
    private final IdentitySettingsService service;
    public IdentitySettingsController(IdentitySettingsService service){this.service=service;}
    public record ChangePassword(String oldPassword,String newPassword){}
    public record ResetOwnPassword(String channel,String target,String code,String newPassword){}
    public record RebindContact(String channel,String newTarget,String code){}
    @PostMapping("/change_password")
    public String changePassword(@RequestBody ChangePassword input){
        return service.changePassword(input.oldPassword(),input.newPassword());
    }
    @PostMapping("/reset_own_password_by_code")
    public String resetPassword(@RequestBody ResetOwnPassword input){
        return service.resetOwnPasswordByCode(input.channel(),input.target(),input.code(),input.newPassword());
    }
    @PostMapping("/rebind_contact")
    public String rebind(@RequestBody RebindContact input){
        return service.rebindContact(input.channel(),input.newTarget(),input.code());
    }
    // Legacy reset_contact_password is service_role only; deliberately NO public route.
}
