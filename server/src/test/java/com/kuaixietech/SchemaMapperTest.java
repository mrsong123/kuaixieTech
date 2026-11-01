package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercise actual MyBatis statement registration and parameter binding without database writes. */
class SchemaMapperTest {
  Configuration config;
  @BeforeEach void load() throws Exception {
    config=new Configuration();
    for(String resource:new String[]{"mapper/account/AuthLoginMapper.xml","mapper/account/AccountCentreMapper.xml","mapper/portal/PortalMapper.xml","mapper/catalog/PublicCatalogMapper.xml","mapper/trading/IntentOrderMapper.xml"}) {
      try(InputStream in=getClass().getClassLoader().getResourceAsStream(resource)) {
        assertNotNull(in,resource);
        new XMLMapperBuilder(in,config,resource,config.getSqlFragments()).parse();
      }
    }
  }
  String sql(String name,Map<String,Object> params) {
    return config.getMappedStatement("com.kuaixietech.mapper."+name).getBoundSql(params).getSql().replaceAll("\\s+"," ").trim();
  }
  @Test void phoneIdentityDoesNotRequireInventedCountryColumn() {
    var input=new HashMap<String,Object>();
    input.put("channel","phone");input.put("contact","2025550123");input.put("country","+1");
    String international=sql("account.AuthLoginMapper.findProfile",input);
    assertTrue(international.contains("CONCAT(?,?)"));
    assertFalse(international.contains("phone_country_code"));
    input.put("country","+86");
    assertFalse(sql("account.AuthLoginMapper.findProfile",input).contains("CONCAT"));
    input.put("channel","email");
    String email=sql("account.AuthLoginMapper.findProfile",input);
    assertTrue(email.contains("AND email=?"));
    assertFalse(email.contains("AND phone="));
  }
  @Test void registrationAndLoginUseTheSameInternationalPhoneEncoding() {
    var params=new HashMap<String,Object>();
    params.put("phone","2025550123");params.put("country","+1");
    assertTrue(sql("account.AuthLoginMapper.insertUser",params).contains("CONCAT(?,?)"));
    assertTrue(sql("account.AuthLoginMapper.insertAdmin",params).contains("CONCAT(?,?)"));
    params.put("phone",null);
    assertFalse(sql("account.AuthLoginMapper.insertUser",params).contains("CONCAT"));
    assertFalse(config.hasStatement("com.kuaixietech.mapper.account.AuthLoginMapper.ensureCredentialTable"));
    assertFalse(config.hasStatement("com.kuaixietech.mapper.account.AuthLoginMapper.ensureCountryColumn"));
  }
  @Test void allPortalQueriesAreBoundAndPublicQueriesHideUnreviewedAndPrivateData() {
    for(String method:new String[]{"publicDemands","communityFeed","profileRole","pendingDemands","pendingTrips","pendingPosts"})
      assertTrue(config.hasStatement("com.kuaixietech.mapper.portal.PortalMapper."+method));
    String demands=sql("portal.PortalMapper.publicDemands",Map.of("limit",30));
    assertTrue(demands.contains("d.visibility='public'"));
    assertTrue(demands.contains("d.status='open'"));
    assertFalse(demands.contains("phone"));
    assertTrue(sql("portal.PortalMapper.communityFeed",Map.of("limit",20)).contains("s.status='approved'"));
  }
  @Test void catalogKeepsAllCountryAssociationsWithoutDuplicatingCategoryRows() {
    String sql=sql("catalog.PublicCatalogMapper.categories",Map.of("limit",100,"countryCode","JP"));
    assertTrue(sql.contains("JSON_ARRAYAGG(cc.country_code)"));
    assertTrue(sql.contains("AND EXISTS"));
    assertFalse(java.util.regex.Pattern.compile("\\bc\\.country\\b").matcher(sql).find());
  }
  @Test void personalOrderAndNotificationQueriesBindOwnership(){
    var params=Map.<String,Object>of("user","user","id","record","role","carrier","limit",20,"offset",0);
    String detail=sql("account.AccountCentreMapper.order",params);
    assertTrue(detail.contains("AND (buyer_id=? OR vendor_id=?)"));
    assertFalse(detail.contains("contact_phone"));
    String list=sql("account.AccountCentreMapper.orders",params);
    assertTrue(list.contains("WHERE vendor_id=?"));
    assertTrue(sql("account.AccountCentreMapper.readNotification",params).contains("AND user_id=?"));
  }
  @Test void allMapperXmlFilesRegisterWithoutDatabaseAccess() throws Exception {
    Configuration all=new Configuration();
    try(var paths=java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources/mapper"))) {
      for(var path:paths.filter(p->p.toString().endsWith(".xml")).toList()) {
        try(InputStream in=java.nio.file.Files.newInputStream(path)) {
          new XMLMapperBuilder(in,all,path.toString(),all.getSqlFragments()).parse();
        }
      }
    }
    assertTrue(all.hasStatement("com.kuaixietech.mapper.portal.PortalMapper.publicDemands"));
    assertTrue(all.hasStatement("com.kuaixietech.mapper.account.AccountCentreMapper.orders"));
  }
  @Test void intentInsertBindsActualRecordValuesAndEscrow() throws Exception {
    var input=new com.kuaixietech.entity.portal.IntentInput("b080aa30-2565-4dde-9801-24bcf8943a7b","Camera",null,1,"piece",null,new java.math.BigDecimal("1000"),null,"Buyer","13800138000","Address",null,"yes",null,null);
    var price=com.kuaixietech.service.trading.support.IntentPricing.quote(new java.math.BigDecimal("1000"),"yes");
    var params=Map.<String,Object>of("id","order","orderNo","DDorder","buyer","buyer","vendor","carrier","input",input,"price",price,"snapshot","{}","intentHours",24);
    var mapped=config.getMappedStatement("com.kuaixietech.mapper.trading.IntentOrderMapper.insertOrder");
    var bound=mapped.getBoundSql(params);
    var statement=org.mockito.Mockito.mock(java.sql.PreparedStatement.class);
    new org.apache.ibatis.scripting.defaults.DefaultParameterHandler(mapped,params,bound).setParameters(statement);
    org.mockito.Mockito.verify(statement).setBigDecimal(org.mockito.ArgumentMatchers.anyInt(),org.mockito.ArgumentMatchers.eq(new java.math.BigDecimal("1060.00")));
    org.mockito.Mockito.verify(statement).setBigDecimal(org.mockito.ArgumentMatchers.anyInt(),org.mockito.ArgumentMatchers.eq(new java.math.BigDecimal("10.00")));
    assertTrue(bound.getSql().contains("'requesting','frozen','unpaid'"));
  }
}
