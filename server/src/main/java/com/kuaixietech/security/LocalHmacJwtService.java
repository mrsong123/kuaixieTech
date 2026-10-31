package com.kuaixietech.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Issues and verifies HS256 tokens with app.jwt-secret. Passwords never go into the token. */
@Service
public class LocalHmacJwtService {
  public static final String ISSUER="kuaixietech-local";
  public static final String AUDIENCE="kuaixietech";
  private final byte[] secret;
  public LocalHmacJwtService(@Value("${app.jwt-secret:}") String secret){
    this.secret=secret==null?new byte[0]:secret.getBytes(StandardCharsets.UTF_8);
  }
  public boolean available(){return secret.length>=32;}
  public SecretKey secretKey(){return new SecretKeySpec(secret,"HmacSHA256");}
  public String issue(String userId){
    if(!available())throw new IllegalStateException("LOCAL_JWT_SECRET_MISSING");
    if(userId==null||!userId.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
      throw new IllegalArgumentException("INVALID_SUBJECT");
    Instant now=Instant.now();
    JWTClaimsSet claims=new JWTClaimsSet.Builder()
        .subject(userId)
        .issuer(ISSUER)
        .audience(AUDIENCE)
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusSeconds(8*3600)))
        .jwtID(UUID.randomUUID().toString())
        .build();
    try{
      SignedJWT jwt=new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),claims);
      jwt.sign(new MACSigner(secret));
      return jwt.serialize();
    }catch(JOSEException e){throw new IllegalStateException("LOCAL_JWT_SIGN_FAILED",e);}
  }
}
