package com.kuaixietech.service.account.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Single-use image codes kept in memory. The answer is never returned as text. */
@Service
public class AuthCaptchaService {
  private static final char[] ALPHABET="ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
  private final SecureRandom random=new SecureRandom();
  private final ConcurrentHashMap<String,Challenge> challenges=new ConcurrentHashMap<>();

  public Map<String,String> issue(){
    long now=System.currentTimeMillis();
    challenges.entrySet().removeIf(entry->entry.getValue().expiresAt()<now);
    StringBuilder text=new StringBuilder(4);
    for(int i=0;i<4;i++) text.append(ALPHABET[random.nextInt(ALPHABET.length)]);
    String id=UUID.randomUUID().toString();
    challenges.put(id,new Challenge(text.toString(),now+120_000));
    return Map.of("id",id,"image",png(text.toString()));
  }

  public void consume(String id,String answer){
    if(id==null||answer==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"CAPTCHA_FAILED");
    Challenge found=challenges.remove(id.trim());
    String given=answer.trim().toUpperCase(Locale.ROOT);
    if(found==null||found.expiresAt()<System.currentTimeMillis()||!found.answer().equals(given)){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"CAPTCHA_FAILED");
    }
  }

  private String png(String text){
    BufferedImage image=new BufferedImage(128,44,BufferedImage.TYPE_INT_RGB);
    Graphics2D g=image.createGraphics();
    try{
      g.setColor(new Color(248,250,252));
      g.fillRect(0,0,128,44);
      g.setColor(new Color(186,214,232));
      g.drawLine(6,32,122,10);
      g.drawLine(8,12,118,34);
      g.setColor(new Color(15,23,42));
      g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,22));
      g.drawString(text,16,30);
    }finally{
      g.dispose();
    }
    try{
      ByteArrayOutputStream out=new ByteArrayOutputStream();
      ImageIO.write(image,"png",out);
      return "data:image/png;base64,"+Base64.getEncoder().encodeToString(out.toByteArray());
    }catch(Exception ex){
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"CAPTCHA_FAILED");
    }
  }

  private record Challenge(String answer,long expiresAt){}
}
