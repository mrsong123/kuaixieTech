package com.kuaixietech.security;

import java.net.URI;
import javax.crypto.SecretKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Public GET catalog stays anonymous. Login is anonymous POST.
 * Every other route requires a verified JWT subject (UUID in sub).
 * Headers such as X-User-Id are never trusted.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(VerifiedJwtSettings.class)
public class ApiSecurityConfig {
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http,VerifiedJwtSettings jwt,DomainAccessGuard guard,LocalHmacJwtService localJwt) throws Exception {
        http.csrf(csrf->csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(b->b.disable())
            .formLogin(f->f.disable());
        boolean remote=jwt.isEnabled();
        boolean local=localJwt.available();
        if(remote) verifyConfiguration(jwt);
        if(remote||local){
            http.addFilterAfter(new ActiveProfileFilter(guard),BearerTokenAuthenticationFilter.class);
            http.authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/payment-callback/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/public/**","/public/**","/api/auth/captcha","/auth/captcha").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/auth/login","/auth/login","/api/auth/wechat","/auth/wechat","/api/auth/send-code","/auth/send-code","/api/auth/register","/auth/register","/api/auth/reset-password","/auth/reset-password","/api/auth/recovery-help","/auth/recovery-help").permitAll()
                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2->oauth2.jwt(Customizer.withDefaults()));
        } else {
            http.authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/payment-callback/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/public/**","/public/**","/api/auth/captcha","/auth/captcha").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/auth/login","/auth/login","/api/auth/wechat","/auth/wechat","/api/auth/send-code","/auth/send-code","/api/auth/register","/auth/register","/api/auth/reset-password","/auth/reset-password","/api/auth/recovery-help","/auth/recovery-help").permitAll()
                .anyRequest().denyAll());
        }
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix="suixinggou.auth.jwt",name="enabled",havingValue="true")
    JwtDecoder verifiedJwtDecoder(VerifiedJwtSettings settings) {
        verifyConfiguration(settings);
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withJwkSetUri(settings.getJwkSetUri()).build();
        OAuth2TokenValidator<Jwt> withIssuer=JwtValidators.createDefaultWithIssuer(settings.getIssuer());
        OAuth2TokenValidator<Jwt> withAudience=token->
             token.getAudience()!=null&&token.getAudience().contains(settings.getAudience())
             ?OAuth2TokenValidatorResult.success()
             :OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Incorrect audience",null));
        OAuth2TokenValidator<Jwt> withUuidSubject=uuidSubject();
        decoder.setJwtValidator(token->{
            OAuth2TokenValidatorResult result=withIssuer.validate(token);
            if(result.hasErrors())return result;
            result=withAudience.validate(token);
            return result.hasErrors()?result:withUuidSubject.validate(token);
        });
        return decoder;
    }

    @Bean
    @ConditionalOnProperty(prefix="suixinggou.auth.jwt",name="enabled",havingValue="false",matchIfMissing=true)
    JwtDecoder localHmacJwtDecoder(LocalHmacJwtService localJwt) {
        if(!localJwt.available()) return token->{throw new JwtException("LOCAL_JWT_SECRET_MISSING");};
        SecretKey key=localJwt.secretKey();
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> defaults=JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> subject=uuidSubject();
        decoder.setJwtValidator(token->{
            OAuth2TokenValidatorResult result=defaults.validate(token);
            return result.hasErrors()?result:subject.validate(token);
        });
        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> uuidSubject(){
        return token-> token.getSubject()!=null&&token.getSubject().matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
            ?OAuth2TokenValidatorResult.success()
            :OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Invalid subject",null));
    }

    private static void verifyConfiguration(VerifiedJwtSettings jwt){
        if(jwt.getIssuer()==null||jwt.getIssuer().isBlank()||jwt.getAudience()==null||jwt.getAudience().isBlank())
            throw new IllegalArgumentException("JWT issuer and audience are required");
        try {
            URI jwk=URI.create(jwt.getJwkSetUri());
            if(!"https".equalsIgnoreCase(jwk.getScheme())||jwk.getHost()==null||jwk.getUserInfo()!=null)
                throw new IllegalArgumentException("JWK URI must be HTTPS");
            URI issuer=URI.create(jwt.getIssuer());
            if(!"https".equalsIgnoreCase(issuer.getScheme())||issuer.getHost()==null)
                throw new IllegalArgumentException("JWT issuer must be an HTTPS URL");
        } catch(NullPointerException e){throw new IllegalArgumentException("JWT issuer/JWK URL missing",e);}
    }
}
