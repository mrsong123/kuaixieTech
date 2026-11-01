package com.kuaixietech.service.operations.impl;

import com.kuaixietech.mapper.operations.AdminConsoleMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.security.TrustedActor;
import com.kuaixietech.service.communication.PrivacyReleaseMigrationService;
import com.kuaixietech.service.community.CommunityMigrationService;
import com.kuaixietech.service.operations.ErrorLogMigrationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminConsoleService {
  private static final Set<String> KINDS = Set.of(
      "carrierKyc","buyerKyc","enterpriseKyc","trips","proofRecipes","products","categories","errorLogs",
      "orders","cancels","disputes","invoices","privacyPool","pools","demands","notifyQueue","withdrawals",
      "community","users","finance","invite","broadcast","channelConfigs","locales","adminLogs");
  private final AdminConsoleMapper mapper;
  private final DomainAccessGuard guard;
  private final LegacyAdminRpcService admin;
  private final CommunityMigrationService community;
  private final PrivacyReleaseMigrationService privacy;
  private final ErrorLogMigrationService errors;
  public AdminConsoleService(AdminConsoleMapper mapper,DomainAccessGuard guard,LegacyAdminRpcService admin,
      CommunityMigrationService community,PrivacyReleaseMigrationService privacy,ErrorLogMigrationService errors){
    this.mapper=mapper;this.guard=guard;this.admin=admin;this.community=community;this.privacy=privacy;this.errors=errors;
  }
  @Transactional(readOnly=true)
  public List<Map<String,Object>> list(String kind,Integer limit,String keyword){return list(kind,limit,keyword,null);}
  @Transactional(readOnly=true)
  public List<Map<String,Object>> list(String kind,Integer limit,String keyword,Integer offset){
    guard.requireAdmin();
    if(kind==null||!KINDS.contains(kind)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"unknown admin console kind");
    int n=limit==null?80:Math.min(Math.max(limit,1),200);
    int start=offset==null?0:offset;
    if(start<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid admin console offset");
    return switch(kind){
      case "carrierKyc" -> mapper.carrierKyc(n,start);
      case "buyerKyc" -> mapper.buyerKyc(n,start);
      case "enterpriseKyc" -> mapper.enterpriseKyc(n,start);
      case "trips" -> mapper.trips(n,start);
      case "proofRecipes" -> mapper.proofRecipes(n,start);
      case "products" -> mapper.products(n,start);
      case "categories" -> mapper.categories(n,start);
      case "errorLogs" -> mapper.errorLogs(n,start);
      case "orders" -> mapper.orders(n,start);
      case "cancels" -> mapper.cancels(n,start);
      case "disputes" -> mapper.disputes(n,start);
      case "invoices" -> mapper.invoices(n,start);
      case "privacyPool" -> mapper.privacyPool(n,start);
      case "pools" -> mapper.pools(n,start);
      case "demands" -> mapper.demands(n,start);
      case "notifyQueue" -> mapper.notifyQueue(n,start);
      case "withdrawals" -> mapper.withdrawals(n,start);
      case "community" -> mapper.community(n,start);
      case "users" -> mapper.users(n,start,keyword==null||keyword.isBlank()?null:keyword.trim());
      case "finance" -> mapper.finance(n,start);
      case "invite" -> mapper.invite(n,start);
      case "broadcast" -> mapper.broadcast(n,start);
      case "channelConfigs" -> mapper.channelConfigs(n,start);
      case "locales" -> mapper.locales(n,start);
      case "adminLogs" -> mapper.adminLogs(n,start);
      default -> List.of();
    };
  }
  @Transactional
  public String setCarrierVerify(String userId,boolean approve,String note){
    String n=note==null?"":note.trim();
    if(!approve && n.isEmpty())return "ERR:NO_NOTE";
    if(approve && n.isEmpty()) n="运营通过";
    admin.adminGuard(approve?"carrier_kyc_approved":"carrier_kyc_rejected","profile",userId,n);
    return mapper.setCarrierVerify(userId,approve,n)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String setTripVerify(String tripId,boolean approve,String note){
    String n=note==null?"":note.trim();
    if(!approve && n.isEmpty()) n="行程核验未通过";
    if(approve && n.isEmpty()) n="运营通过";
    admin.adminGuard(approve?"trip_approved":"trip_rejected","trip",tripId,n);
    return mapper.setTripVerify(tripId,approve,n)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String forceOffShelf(String productId){
    if(productId==null||productId.isBlank())return "ERR:NO_ID";
    admin.adminGuard("product_off_shelf","product",productId,"强制下架");
    return mapper.forceOffShelf(productId)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String deactivateCategory(Long id){
    if(id==null||id<=0)return "ERR:NO_ID";
    admin.adminGuard("category_deactivated","category",String.valueOf(id),"停用品类");
    return mapper.deactivateCategory(id)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String createCategory(String name,Long parentId,Integer level){
    String n=name==null?"":name.trim();
    if(n.isEmpty()||n.length()>64)return "ERR:BAD_NAME";
    int lv=level==null?1:level;
    if(lv<1||lv>3)return "ERR:BAD_LEVEL";
    admin.adminGuard("category_created","category",n,"level="+lv);
    mapper.createCategory(n,parentId,lv);
    return "OK";
  }
  @Transactional
  public String createInvite(String code,String note){
    String c=code==null?"":code.trim().toUpperCase();
    if(!c.matches("[A-Z0-9]{6,16}"))return "ERR:BAD_CODE";
    String actor=admin.adminGuard("create_invite","invite_code",c,note==null?"":note.trim());
    mapper.createInvite(c,note==null?"":note.trim(),actor);
    return "OK";
  }
  @Transactional
  public String upsertRecipe(String id,String tripType,String transportMode,String countryCode,String items,String note){
    if(tripType==null||tripType.isBlank()||transportMode==null||transportMode.isBlank())return "ERR:BAD_MODE";
    String country=(countryCode==null||countryCode.isBlank())?"*":countryCode.trim().toUpperCase();
    String json=items==null||items.isBlank()?"[]":items.trim();
    if(!(json.startsWith("[")&&json.endsWith("]")))return "ERR:BAD_ITEMS";
    if(id==null||id.isBlank()){
      String nid=UUID.randomUUID().toString();
      admin.adminGuard("recipe_create","proof_recipe",nid,transportMode);
      mapper.insertRecipe(nid,tripType,transportMode,country,json,note);
      return "OK:"+nid;
    }
    admin.adminGuard("recipe_update","proof_recipe",id,transportMode);
    return mapper.updateRecipe(id,tripType,transportMode,country,json,note)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String toggleRecipe(String id,boolean active){
    if(id==null||id.isBlank())return "ERR:NO_ID";
    admin.adminGuard("recipe_toggle","proof_recipe",id,String.valueOf(active));
    return mapper.toggleRecipe(id,active)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String toggleChannel(String id,boolean enabled){
    if(id==null||id.isBlank())return "ERR:NO_ID";
    admin.adminGuard("channel_toggle","channel_config",id,String.valueOf(enabled));
    return mapper.toggleChannel(id,enabled)==1?"OK":"ERR:NO_ROW";
  }
  @Transactional
  public String removePost(String postId,String reason){
    String actor=guard.requireAdmin();
    return community.adminRemovePost(actor,postId,reason);
  }
  @Transactional
  public String releasePrivacy(){
    guard.requireAdmin();
    admin.adminGuard("privacy_release_expired","system","","释放过期隐私号");
    return privacy.releaseExpired();
  }
  @Transactional
  public String resolveError(Long id){
    if(id==null||id<=0)return "ERR:NO_ID";
    return errors.resolve(TrustedActor.currentUserId(),id);
  }
}
