package com.kuaixietech.service.catalog.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.security.DomainAccessGuard;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Owner-scoped publishing against the existing CNY on/off product schema. */
@Service
public class ProductPublishingService {
  public record Input(@NotBlank @Size(max=255) String name,@Size(max=255) String spec,
      @NotBlank @Size(max=64) String region,@NotNull @DecimalMin("0.01") @DecimalMax("9999999999.99") @Digits(integer=10,fraction=2) BigDecimal price,
      @NotNull @Min(0) @Max(1000000) Integer stock,@Positive Long categoryId,
      @NotNull @Size(max=9) List<@NotBlank @Size(max=800) String> imageUrls,@Size(max=1024) String note,@Min(0) @Max(1000000) Integer expectedStock){
    public Input(String name,String spec,String region,BigDecimal price,Integer stock,Long categoryId,List<String> imageUrls,String note){this(name,spec,region,price,stock,categoryId,imageUrls,note,null);}
  }
  public record State(@NotBlank @Pattern(regexp="on|off") String status){}
  private final JdbcTemplate db;private final DomainAccessGuard guard;private final ObjectMapper json;
  private com.kuaixietech.storage.UploadBindings uploads;
  @org.springframework.beans.factory.annotation.Autowired public void uploadBindings(com.kuaixietech.storage.UploadBindings uploads){this.uploads=uploads;}
  private void bindImages(String id,String owner,List<String> paths){if(uploads!=null)uploads.replaceProduct(id,owner,paths);else if(!com.kuaixietech.storage.UploadBindings.ids(paths).isEmpty())throw new IllegalStateException("PRODUCT_IMAGE_BINDING_UNAVAILABLE");}
  private final Set<String> mediaHosts=new HashSet<>();
  public ProductPublishingService(JdbcTemplate db,DomainAccessGuard guard,ObjectMapper json,
      @Value("${kuaixie.portal.allowed-media-hosts:}") String hosts){
    this.db=db;this.guard=guard;this.json=json;
    for(String host:hosts.split(","))if(!host.isBlank())mediaHosts.add(host.trim().toLowerCase(Locale.ROOT));
  }
  private ResponseStatusException invalid(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
  private String images(Input input){
    for(String image:input.imageUrls()){
      if(com.kuaixietech.storage.UploadBindings.imageId(image)!=null)continue;
      try{URI uri=URI.create(image);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getFragment()!=null||uri.getPath().startsWith("/public/media/")||(uri.getPort()!=-1&&uri.getPort()!=443)||!mediaHosts.contains(uri.getHost().toLowerCase(Locale.ROOT)))throw new IllegalArgumentException();}
      catch(Exception error){throw invalid("image must use a configured HTTPS upload host");}
    }
    try{return json.writeValueAsString(input.imageUrls());}catch(JsonProcessingException error){throw invalid("invalid images");}
  }
  private void category(Input input){
    if(input.categoryId()!=null&&!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM t_categories WHERE id=? AND active=1)",Boolean.class,input.categoryId())))throw invalid("category is unavailable");
  }
  public List<Map<String,Object>> mine(int limit,int offset){
    String user=guard.requireActiveProfile();
    if(limit<1||limit>100||offset<0)throw invalid("invalid pagination");
    return db.queryForList("SELECT id,CAST(category_id AS CHAR) AS category_id,name,spec,region,price,stock,image_urls,note,status,created_at,updated_at FROM t_products WHERE owner_id=? ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?",user,limit,offset);
  }
  @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Map<String,String> create(Input input){
    String user=guard.requireVerifiedCarrier();String images=images(input);category(input);String id=UUID.randomUUID().toString();
    db.update("INSERT INTO t_products(id,owner_id,category_id,name,spec,region,price,stock,image_urls,note,status) VALUES(?,?,?,?,?,?,?,?,?,?,'off')",id,user,input.categoryId(),input.name().trim(),input.spec(),input.region().trim(),input.price(),input.stock(),images,input.note());
    bindImages(id,user,input.imageUrls());
    return Map.of("id",id,"status","off");
  }
  @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Map<String,String> update(String id,Input input){
    String user=guard.requireVerifiedCarrier();String images=images(input);category(input);
    // Lock ownership before editing. Every edit removes the item from public sale until explicitly relisted.
    var rows=db.queryForList("SELECT id,stock,name,spec,region,category_id,note,image_urls FROM t_products WHERE id=? AND owner_id=? FOR UPDATE",id,user);
    if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"product unavailable");
    Map<String,Object> currentProduct=rows.getFirst();
    if(!db.queryForList("SELECT order_id FROM t_product_order_reservations WHERE product_id=? AND stock_state='held' LIMIT 1",id).isEmpty()){
      boolean same=Objects.equals(currentProduct.get("name"),input.name().trim())&&Objects.equals(currentProduct.get("spec"),input.spec())&&Objects.equals(currentProduct.get("region"),input.region().trim())&&Objects.equals(Objects.toString(currentProduct.get("category_id"),null),input.categoryId()==null?null:input.categoryId().toString())&&Objects.equals(currentProduct.get("note"),input.note());
      try{same= same&&json.readTree(Objects.toString(currentProduct.get("image_urls"),"[]")).equals(json.readTree(images));}catch(JsonProcessingException e){throw new IllegalStateException("Invalid product images",e);}
      if(!same)throw new ResponseStatusException(HttpStatus.CONFLICT,"PRODUCT_IDENTITY_HELD");
    }
    Object currentStock=currentProduct.get("stock");
    if(currentStock instanceof Number current&&current.intValue()!=input.stock()&&!db.queryForList("SELECT order_id FROM t_product_order_reservations WHERE product_id=? LIMIT 1",id).isEmpty()&&(input.expectedStock()==null||input.expectedStock()!=current.intValue()))throw new ResponseStatusException(HttpStatus.CONFLICT,"PRODUCT_STOCK_CHANGED_REFRESH");
    db.update("UPDATE t_products SET category_id=?,name=?,spec=?,region=?,price=?,stock=?,image_urls=?,note=?,status='off' WHERE id=? AND owner_id=?",input.categoryId(),input.name().trim(),input.spec(),input.region().trim(),input.price(),input.stock(),images,input.note(),id,user);
    bindImages(id,user,input.imageUrls());
    return Map.of("id",id,"status","off");
  }
  @Transactional public Map<String,String> state(String id,State input){
    // An active owner may always remove a listing, including after carrier verification is revoked.
    String user=input.status().equals("on")?guard.requireVerifiedCarrier():guard.requireActiveProfile();
    var rows=db.queryForList("SELECT stock,category_id FROM t_products WHERE id=? AND owner_id=? FOR UPDATE",id,user);
    if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"product unavailable");
    if(input.status().equals("on")){
      Map<String,Object> row=rows.getFirst();
      if(((Number)row.get("stock")).intValue()<1)throw invalid("stock must be positive before listing");
      if(row.get("category_id")!=null&&!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM t_categories WHERE id=? AND active=1)",Boolean.class,row.get("category_id"))))throw invalid("category is unavailable");
    }
    db.update("UPDATE t_products SET status=? WHERE id=? AND owner_id=?",input.status(),id,user);
    return Map.of("id",id,"status",input.status());
  }
}
