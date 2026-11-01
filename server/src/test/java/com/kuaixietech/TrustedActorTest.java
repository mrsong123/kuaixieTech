package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import com.kuaixietech.security.TrustedActor;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
class TrustedActorTest {
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void uppercaseUuidClaimCannotCreateADifferentTradingIdentity(){SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated("ABCDEF01-2345-4678-9ABC-DEF012345678","test",List.of()));assertEquals("abcdef01-2345-4678-9abc-def012345678",TrustedActor.currentUserId());}
 @Test void malformedOrAnonymousClaimsRemainRejected(){assertThrows(AccessDeniedException.class,TrustedActor::currentUserId);SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated("1-2-3-4-5","test",List.of()));assertThrows(AccessDeniedException.class,TrustedActor::currentUserId);}
}
