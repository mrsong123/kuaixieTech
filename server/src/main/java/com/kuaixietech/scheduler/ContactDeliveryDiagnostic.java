package com.kuaixietech.scheduler;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.web.server.ResponseStatusException;

/** Safe operational facts only: never include targets, OTPs, SQL text or credential values. */
public record ContactDeliveryDiagnostic(String channel, String state, String stage, String reasonCode,
    String exceptionType, String sqlState, Integer databaseError, String missingTable, Instant observedAt) {
    private static final Pattern TABLE = Pattern.compile("(?i)(?:table|relation)\\s+['`\"](?:[^'`\"]*\\.)?(t_[a-z0-9_]+)['`\"]");
    public static ContactDeliveryDiagnostic state(String channel, String state, String stage, String code) {
        return new ContactDeliveryDiagnostic(channel,state,stage,code,null,null,null,null,Instant.now());
    }
    public boolean failed() { return !Set.of("ready","idle","paused","not_checked","disabled").contains(state); }
    public boolean paused() { return state.equals("paused"); }
    public static ContactDeliveryDiagnostic failure(String channel,String stage,RuntimeException error) {
        String code = safeCode(error),state = "unexpected_error",sqlState = null,table = null;
        Integer vendor = null; SQLException sql = null;
        for(Throwable cause=error;cause!=null;cause=cause.getCause()) {
            if(cause instanceof SQLException found){sql=found;break;}
            if(cause==cause.getCause())break;
        }
        if(sql!=null){
            sqlState=sql.getSQLState();vendor=sql.getErrorCode();
            if("42S02".equals(sqlState)||vendor==1146){state="migration_required";code="CONTACT_SCHEMA_MISSING";var match=TABLE.matcher(String.valueOf(sql.getMessage()));if(match.find())table=match.group(1);}
            else if("42S22".equals(sqlState)||vendor==1054){state="migration_required";code="CONTACT_SCHEMA_INCOMPATIBLE";}
            else if(vendor==1049){state="database_unavailable";code="CONTACT_DATABASE_NOT_FOUND";}
            else if(sqlState!=null&&sqlState.startsWith("08")){state="database_unavailable";code="CONTACT_DATABASE_UNAVAILABLE";}
            else if("28000".equals(sqlState)||vendor==1045){state="database_unavailable";code="CONTACT_DATABASE_ACCESS_DENIED";}
            else {state="database_error";code="CONTACT_DATABASE_ERROR";}
        } else if(code.equals("COUPLER_LIVE_MODE_REQUIRED")||code.equals("SERVICE_LIVE_MODE_REQUIRED")||code.equals("COUPLER_FEATURE_DISABLED"))state="paused";
        else if(code.contains("CREDENTIAL_UNREADABLE")||code.contains("CREDENTIAL_UNAVAILABLE"))state="credentials_unreadable";
        else if(code.contains("ENCRYPTION_KEY")||code.contains("ENV_REFERENCE_MISSING"))state="credentials_missing";
        else if(code.contains("PROVIDER_NOT_CONFIGURED"))state="idle";
        else if(code.contains("UNCONFIRMED"))state="dispatch_unconfirmed";
        else if(code.contains("PROVIDER_REJECTED")||code.contains("INPUT_REJECTED"))state="dispatch_failed";
        else if(code.startsWith("INTEGRATION_")||code.startsWith("COUPLER_")||code.contains("MULTIPLE_ACTIVE")||stage.equals("configuration")&&(code.startsWith("SMS_")||code.startsWith("EMAIL_")))state="configuration_invalid";
        if(error instanceof ContactDeliveryAttempt && code.contains("UNCONFIRMED"))state="dispatch_unconfirmed";
        // Preserve the durable-attempt uncertainty even if its underlying receipt write failed in SQL.
        if(error instanceof ContactDeliveryAttempt attempt){code=attempt.getMessage();if(code.contains("UNCONFIRMED"))state="dispatch_unconfirmed";}
        return new ContactDeliveryDiagnostic(channel,state,stage,code,error.getClass().getSimpleName(),sqlState,vendor,table,Instant.now());
    }
    private static String safeCode(RuntimeException error){
        String text=error instanceof ResponseStatusException status?status.getReason():error.getMessage();
        return text!=null&&text.matches("(?:CONTACT|SMS|EMAIL|INTEGRATION|COUPLER|SERVICE)_[A-Z0-9_]{1,100}")?text:"CONTACT_UNEXPECTED_ERROR";
    }
}
