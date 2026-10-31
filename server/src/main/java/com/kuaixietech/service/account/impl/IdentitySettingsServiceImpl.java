package com.kuaixietech.service.account.impl;

import com.kuaixietech.identity.ContactAndPasswordRules;
import com.kuaixietech.identity.IdentityProviderClient;
import com.kuaixietech.mapper.account.IdentitySettingsMapper;
import com.kuaixietech.security.TrustedActor;
import com.kuaixietech.service.account.IdentitySettingsService;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Identity authority is the configured IdP; this service owns application contact data + OTP.
 * Never accepts a userId from the client and never stores a password in MySQL.
 */
@Service
public class IdentitySettingsServiceImpl implements IdentitySettingsService {
    private final IdentitySettingsMapper db;
    private final IdentityProviderClient provider;
    public IdentitySettingsServiceImpl(IdentitySettingsMapper db,IdentityProviderClient provider){
        this.db=db;this.provider=provider;
    }
    @Override public String changePassword(String oldPassword,String newPassword){
        String userId=TrustedActor.currentUserId();
        if(!ContactAndPasswordRules.validPassword(newPassword))return "ERR:WEAK_PWD";
        if(oldPassword==null||oldPassword.isEmpty())return "ERR:BAD_OLD";
        return translate(provider.changePassword(userId,oldPassword,newPassword));
    }
    @Override @Transactional public String resetOwnPasswordByCode(String channel,String target,String code,String newPassword){
        String userId=TrustedActor.currentUserId();
        if(!ContactAndPasswordRules.validPassword(newPassword))return "ERR:WEAK_PWD";
        String normalized=ContactAndPasswordRules.normalize(channel,target);
        if(normalized==null)return "ERR:BAD_CODE";
        String codeId=validCode(normalized,channel,code);
        if(codeId==null)return "ERR:BAD_CODE";
        // The IdP, not t_profiles, owns the login identifier and its uniqueness.
        String resolved=provider.resolveUserByLoginIdentifier(
            ContactAndPasswordRules.loginIdentifier(channel,normalized));
        if(!userId.equals(resolved))return "ERR:TARGET_MISMATCH";
        String result=translate(provider.resetPassword(userId,newPassword,codeId));
        if(!"OK".equals(result))return result;
        if(db.consumeLockedCode(codeId)!=1)throw new IllegalStateException("OTP was concurrently consumed");
        return "OK";
    }
    @Override @Transactional public String rebindContact(String channel,String newTarget,String code){
        String userId=TrustedActor.currentUserId();
        if(!ContactAndPasswordRules.validChannel(channel))return "ERR:BAD_CHANNEL";
        String normalized=ContactAndPasswordRules.normalize(channel,newTarget);
        if(normalized==null)return "ERR:BAD_TARGET";
        if(db.lockProfile(userId)==null)return "ERR:NO_AUTH";
        if(db.countContactTaken(userId,channel,normalized)>0)return "ERR:TAKEN";
        String codeId=validCode(normalized,channel,code);
        if(codeId==null)return "ERR:BAD_CODE";
        String result=translate(provider.rebindContact(userId,channel,normalized,
              ContactAndPasswordRules.loginIdentifier(channel,normalized),codeId));
        if(!"OK".equals(result))return result;
        if(db.updateContact(userId,channel,normalized)!=1)throw new IllegalStateException("Profile contact update failed");
        if(db.consumeLockedCode(codeId)!=1)throw new IllegalStateException("OTP was concurrently consumed");
        return "OK";
    }
    /** Only internal callers who have a real OTP may invoke this; NO HTTP controller maps this method. */
    @Override @Transactional public boolean resetContactPasswordAfterVerifiedCode(
                  String target,String channel,String code,String newPassword){
        if(!ContactAndPasswordRules.validPassword(newPassword))return false;
        String normalized=ContactAndPasswordRules.normalize(channel,target);
        if(normalized==null)return false;
        String codeId=validCode(normalized,channel,code);
        if(codeId==null)return false;
        String userId=provider.resolveUserByLoginIdentifier(
            ContactAndPasswordRules.loginIdentifier(channel,normalized));
        if(userId==null)return false;
        String result=translate(provider.resetPassword(userId,newPassword,codeId));
        if(!"OK".equals(result))return false;
        if(db.consumeLockedCode(codeId)!=1)throw new IllegalStateException("OTP was concurrently consumed");
        return true;
    }
    private String validCode(String target,String channel,String code){
        if(code==null||code.isBlank())return null;
        return db.lockValidCode(target,channel,code);
    }
    private static String translate(IdentityProviderClient.Outcome outcome){
        return switch(outcome){
            case OK -> "OK";
            case BAD_OLD -> "ERR:BAD_OLD";
            case NO_PASSWORD_SET -> "ERR:NO_PASSWORD_SET";
            case TAKEN -> "ERR:TAKEN";
            case NOT_FOUND -> "ERR:TARGET_MISMATCH";
        };
    }
}
