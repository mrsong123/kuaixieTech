package com.kuaixietech.mapper.operations;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface AdminConsoleMapper {
  List<Map<String,Object>> carrierKyc(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> carrierKyc(int limit){return carrierKyc(limit,0);}
  List<Map<String,Object>> buyerKyc(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> buyerKyc(int limit){return buyerKyc(limit,0);}
  List<Map<String,Object>> enterpriseKyc(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> enterpriseKyc(int limit){return enterpriseKyc(limit,0);}
  List<Map<String,Object>> trips(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> trips(int limit){return trips(limit,0);}
  List<Map<String,Object>> proofRecipes(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> proofRecipes(int limit){return proofRecipes(limit,0);}
  List<Map<String,Object>> products(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> products(int limit){return products(limit,0);}
  List<Map<String,Object>> categories(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> categories(int limit){return categories(limit,0);}
  List<Map<String,Object>> errorLogs(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> errorLogs(int limit){return errorLogs(limit,0);}
  List<Map<String,Object>> orders(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> orders(int limit){return orders(limit,0);}
  List<Map<String,Object>> cancels(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> cancels(int limit){return cancels(limit,0);}
  List<Map<String,Object>> disputes(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> disputes(int limit){return disputes(limit,0);}
  List<Map<String,Object>> invoices(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> invoices(int limit){return invoices(limit,0);}
  List<Map<String,Object>> privacyPool(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> privacyPool(int limit){return privacyPool(limit,0);}
  List<Map<String,Object>> pools(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> pools(int limit){return pools(limit,0);}
  List<Map<String,Object>> demands(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> demands(int limit){return demands(limit,0);}
  List<Map<String,Object>> notifyQueue(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> notifyQueue(int limit){return notifyQueue(limit,0);}
  List<Map<String,Object>> withdrawals(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> withdrawals(int limit){return withdrawals(limit,0);}
  List<Map<String,Object>> community(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> community(int limit){return community(limit,0);}
  List<Map<String,Object>> users(@Param("limit")int limit,@Param("offset")int offset,@Param("keyword")String keyword);
  default List<Map<String,Object>> users(int limit,String keyword){return users(limit,0,keyword);}
  List<Map<String,Object>> finance(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> finance(int limit){return finance(limit,0);}
  List<Map<String,Object>> invite(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> invite(int limit){return invite(limit,0);}
  List<Map<String,Object>> broadcast(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> broadcast(int limit){return broadcast(limit,0);}
  List<Map<String,Object>> channelConfigs(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> channelConfigs(int limit){return channelConfigs(limit,0);}
  List<Map<String,Object>> locales(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> locales(int limit){return locales(limit,0);}
  List<Map<String,Object>> adminLogs(@Param("limit")int limit,@Param("offset")int offset);
  default List<Map<String,Object>> adminLogs(int limit){return adminLogs(limit,0);}
  int setCarrierVerify(@Param("id")String id,@Param("approved")boolean approved,@Param("note")String note);
  int setTripVerify(@Param("id")String id,@Param("approved")boolean approved,@Param("note")String note);
  int forceOffShelf(@Param("id")String id);
  int deactivateCategory(@Param("id")long id);
  int createCategory(@Param("name")String name,@Param("parentId")Long parentId,@Param("level")int level);
  int createInvite(@Param("code")String code,@Param("note")String note,@Param("actor")String actor);
  int insertRecipe(@Param("id")String id,@Param("tripType")String tripType,@Param("transportMode")String transportMode,@Param("countryCode")String countryCode,@Param("items")String items,@Param("note")String note);
  int updateRecipe(@Param("id")String id,@Param("tripType")String tripType,@Param("transportMode")String transportMode,@Param("countryCode")String countryCode,@Param("items")String items,@Param("note")String note);
  int toggleRecipe(@Param("id")String id,@Param("active")boolean active);
  int toggleChannel(@Param("id")String id,@Param("enabled")boolean enabled);
}
