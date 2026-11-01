package com.kuaixietech.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.kuaixietech.coupler.CouplerModeGate;
import com.kuaixietech.scheduler.*;
import com.kuaixietech.security.DomainAccessGuard;
import java.sql.SQLException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

class ContactDeliveryDiagnosticsTest {
    RuntimeException missing(){return new BadSqlGrammarException("probe","secret SQL must not escape",new SQLException("Table 'private_db.t_integration_runtime_state' doesn't exist; secret-value", "42S02",1146));}
    @Test void missingMigrationHasSafeActionableIdentityWithoutSqlOrDatabaseName(){
        var d=ContactDeliveryDiagnostic.failure("sms","configuration",missing());
        assertEquals("migration_required",d.state());assertEquals("CONTACT_SCHEMA_MISSING",d.reasonCode());assertEquals("t_integration_runtime_state",d.missingTable());assertEquals("42S02",d.sqlState());assertEquals(1146,d.databaseError());
        assertFalse(d.toString().contains("private_db"));assertFalse(d.toString().contains("secret"));
    }
    @Test void missingRootKeyDiffersFromUnreadableCipher(){
        var error=assertThrows(IllegalStateException.class,()->new IntegrationCipher("").open("scope","cipher"));
        assertEquals("credentials_missing",ContactDeliveryDiagnostic.failure("email","configuration",error).state());
        assertEquals("credentials_unreadable",ContactDeliveryDiagnostic.failure("email","configuration",new IllegalStateException("INTEGRATION_CREDENTIAL_UNREADABLE")).state());
    }
    @Test void missingTableDoesNotDisableTheOtherChannelOrInvokeLegacy(){
        var sms=mock(SmsOutboxDispatcher.class);var email=mock(EmailOutboxDispatcher.class);var legacy=mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenThrow(missing());when(email.configured()).thenReturn(true);
        var scheduler=new ContactCodeDeliveryScheduler(legacy,sms,email,true);
        assertDoesNotThrow(scheduler::run);assertEquals("degraded",scheduler.snapshot().state());assertEquals("migration_required",scheduler.snapshot().sms().state());verify(email).run();verifyNoInteractions(legacy);
    }
    @Test void deliberatelyPausedModeDoesNotProbeSecretsOrTouchQueue(){
        var config=mock(IntegrationConfiguration.class);var queue=mock(SmsOutboxStore.class);var protocols=mock(SmsProtocols.class);var gate=mock(CouplerModeGate.class);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT,"COUPLER_LIVE_MODE_REQUIRED")).when(gate).requireLiveNewCall("sms_dispatch");
        var sms=new SmsOutboxDispatcher(config,queue,protocols);sms.couplerMode(gate);
        assertFalse(sms.configured());assertEquals("paused",sms.configurationStatus().state());verifyNoInteractions(config,queue,protocols);
    }
    @Test void emptyConfigurationIsIdleAndDisabledNativeConfigurationIsPaused(){
        var config=mock(IntegrationConfiguration.class);when(config.runtime(anyString(),anyString())).thenReturn(null);
        var sms=new SmsOutboxDispatcher(config,mock(SmsOutboxStore.class),mock(SmsProtocols.class));
        assertFalse(sms.configured());assertEquals("idle",sms.configurationStatus().state());
        when(config.exists("aliyun","sms")).thenReturn(true);
        assertFalse(sms.configured());assertEquals("paused",sms.configurationStatus().state());
    }
    @Test void ambiguousReceiptWriteRetainsBlockedAttemptAndReportsUncertainty(){
        var config=mock(IntegrationConfiguration.class);when(config.runtime("aliyun","sms")).thenReturn(Map.of("configured","yes"));when(config.runtime("tencent","sms")).thenReturn(null);
        var queue=mock(SmsOutboxStore.class);when(queue.claim()).thenReturn(Map.of("id",1L,"target","+8613800138000","code","123456"));
        var protocol=mock(SmsProtocols.class);when(protocol.send(anyString(),anyMap(),anyString(),anyString(),anyString())).thenReturn(new SmsProtocols.Receipt(true,"receipt"));
        doThrow(missing()).when(queue).result(any(),anyString(),anyString());
        var error=assertThrows(ContactDeliveryAttempt.class,()->new SmsOutboxDispatcher(config,queue,protocol).run());
        var d=ContactDeliveryDiagnostic.failure("sms","dispatch",error);
        assertEquals("dispatch_unconfirmed",d.state());assertEquals("SMS_DISPATCH_UNCONFIRMED",d.reasonCode());verify(queue,times(1)).claim();verify(protocol,times(1)).send(anyString(),anyMap(),anyString(),anyString(),anyString());
    }
    @Test void healthIsAdminOnlyAndHasNoDispatchSideEffect(){
        var sms=mock(SmsOutboxDispatcher.class);var email=mock(EmailOutboxDispatcher.class);var guard=mock(DomainAccessGuard.class);var db=mock(JdbcTemplate.class);var legacy=mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenReturn(true);when(db.queryForList(anyString())).thenReturn(List.of(Map.of("channel","phone","status","blocked","total",1L)));
        var controller=new ContactDeliveryHealthController(guard,new ContactCodeDeliveryScheduler(legacy,sms,email,true),db);
        var health=controller.health();assertEquals("requires_reconciliation",health.get("state"));assertEquals(false,health.get("providerDeliveryVerified"));verify(guard).requireAdmin();verify(sms,never()).run();verify(email,never()).run();verifyNoInteractions(legacy);
        verify(db).queryForList(argThat(sql->!sql.contains("target")&&!sql.contains("code")&&!sql.contains("field_cipher")));
    }
    @Test void deniedHealthCannotReadConfigurationOrQueue(){
        var guard=mock(DomainAccessGuard.class);when(guard.requireAdmin()).thenThrow(new SecurityException());var scheduler=mock(ContactCodeDeliveryScheduler.class);var db=mock(JdbcTemplate.class);
        assertThrows(SecurityException.class,()->new ContactDeliveryHealthController(guard,scheduler,db).health());verifyNoInteractions(scheduler,db);
    }
    @Test void unknownExceptionMessagesCannotLeakRecipientsOrSecrets(){
        var d=ContactDeliveryDiagnostic.failure("sms","dispatch",new IllegalStateException("buyer@example.test OTP=123456 key=super-secret"));
        assertEquals("CONTACT_UNEXPECTED_ERROR",d.reasonCode());assertFalse(d.toString().contains("123456"));assertFalse(d.toString().contains("super-secret"));
    }
    @Test void missingColumnAndConnectionFailuresHaveDistinctActionCodes(){
        var column=ContactDeliveryDiagnostic.failure("sms","configuration",new BadSqlGrammarException("probe","hidden SQL",new SQLException("Unknown column private_field","42S22",1054)));
        assertEquals("migration_required",column.state());assertEquals("CONTACT_SCHEMA_INCOMPATIBLE",column.reasonCode());assertFalse(column.toString().contains("private_field"));
        var connection=ContactDeliveryDiagnostic.failure("email","configuration",new IllegalStateException(new SQLException("secret host/password","08S01",0)));
        assertEquals("database_unavailable",connection.state());assertEquals("CONTACT_DATABASE_UNAVAILABLE",connection.reasonCode());assertFalse(connection.toString().contains("password"));
    }
    @Test void healthKeepsLastFailedAttemptVisibleAfterConfigurationReadsSuccessfully(){
        var sms=mock(SmsOutboxDispatcher.class);var email=mock(EmailOutboxDispatcher.class);var legacy=mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenReturn(true);doThrow(new ContactDeliveryAttempt("SMS_PROVIDER_REJECTED",null)).when(sms).run();
        var scheduler=new ContactCodeDeliveryScheduler(legacy,sms,email,false);scheduler.run();
        var guard=mock(DomainAccessGuard.class);var db=mock(JdbcTemplate.class);
        assertEquals("degraded",new ContactDeliveryHealthController(guard,scheduler,db).health().get("state"));
    }
    @Test void failedDispatchIsRecordedWhileHealthyEmailContinues(){
        var sms=mock(SmsOutboxDispatcher.class);var email=mock(EmailOutboxDispatcher.class);var legacy=mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenReturn(true);when(email.configured()).thenReturn(true);doThrow(new ContactDeliveryAttempt("SMS_DISPATCH_UNCONFIRMED",null)).when(sms).run();
        var scheduler=new ContactCodeDeliveryScheduler(legacy,sms,email,true);scheduler.run();
        assertEquals("dispatch_unconfirmed",scheduler.snapshot().sms().state());assertEquals("ready",scheduler.snapshot().email().state());assertEquals("degraded",scheduler.snapshot().state());verifyNoInteractions(legacy);
    }
}
