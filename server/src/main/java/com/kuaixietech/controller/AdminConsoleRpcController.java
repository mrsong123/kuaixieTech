package com.kuaixietech.controller;

import com.kuaixietech.service.operations.impl.AdminConsoleService;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rpc")
public class AdminConsoleRpcController {
  private final AdminConsoleService console;
  private final LegacyAdminRpcService legacy;
  public AdminConsoleRpcController(AdminConsoleService console,LegacyAdminRpcService legacy){
    this.console=console;this.legacy=legacy;
  }
  public record ListBody(String kind,Integer limit,String keyword,Integer offset){
    public ListBody(String kind,Integer limit,String keyword){this(kind,limit,keyword,null);}
  }
  public record VerifyBody(String userId,String tripId,boolean approve,String note){}
  public record IdBody(String id){}
  public record BanBody(String userId,boolean ban,String reason){}
  public record BroadcastBody(String title,String body,String scope,List<String> userIds){}
  public record DemandCloseBody(String demandId,String reason){}
  public record ScoreBody(String userId,Integer delta,String note){}
  public record PoolFormBody(String tripId){}
  public record CategoryCreate(String name,Long parentId,Integer level){}
  public record CategoryId(Long id){}
  public record InviteBody(String code,String note){}
  public record RecipeBody(String id,String tripType,String transportMode,String countryCode,String items,String note){}
  public record RecipeToggle(String id,boolean active){}
  public record ChannelToggle(String id,boolean enabled){}
  public record PostRemove(String postId,String reason){}
  public record ErrorId(Long id){}

  @PostMapping("/admin_console_list") public List<Map<String,Object>> list(@RequestBody ListBody b){return console.list(b.kind(),b.limit(),b.keyword(),b.offset());}
  @PostMapping("/admin_set_carrier_verify") public String carrier(@RequestBody VerifyBody b){return console.setCarrierVerify(b.userId(),b.approve(),b.note());}
  @PostMapping("/admin_set_trip_verify") public String trip(@RequestBody VerifyBody b){return console.setTripVerify(b.tripId(),b.approve(),b.note());}
  @PostMapping("/admin_force_off_shelf") public String off(@RequestBody IdBody b){return console.forceOffShelf(b.id());}
  @PostMapping("/admin_ban_user") public String ban(@RequestBody BanBody b){return legacy.banUser(b.userId(),b.ban(),b.reason());}
  @PostMapping("/admin_broadcast") public String broadcast(@RequestBody BroadcastBody b){return legacy.broadcast(b.title(),b.body(),b.scope(),b.userIds());}
  @PostMapping("/admin_close_demand") public String closeDemand(@RequestBody DemandCloseBody b){return legacy.closeDemand(b.demandId(),b.reason());}
  @PostMapping("/admin_fix_score") public String score(@RequestBody ScoreBody b){return legacy.fixScore(b.userId(),b.delta(),b.note());}
  @PostMapping("/admin_force_form_pool") public String form(@RequestBody PoolFormBody b){return legacy.forceFormPool(b.tripId());}
  @PostMapping("/admin_create_category") public String createCategory(@RequestBody CategoryCreate b){return console.createCategory(b.name(),b.parentId(),b.level());}
  @PostMapping("/admin_deactivate_category") public String deactivateCategory(@RequestBody CategoryId b){return console.deactivateCategory(b.id());}
  @PostMapping("/admin_create_invite") public String createInvite(@RequestBody InviteBody b){return console.createInvite(b.code(),b.note());}
  @PostMapping("/admin_upsert_recipe") public String upsertRecipe(@RequestBody RecipeBody b){return console.upsertRecipe(b.id(),b.tripType(),b.transportMode(),b.countryCode(),b.items(),b.note());}
  @PostMapping("/admin_toggle_recipe") public String toggleRecipe(@RequestBody RecipeToggle b){return console.toggleRecipe(b.id(),b.active());}
  @PostMapping("/admin_toggle_channel") public String toggleChannel(@RequestBody ChannelToggle b){return console.toggleChannel(b.id(),b.enabled());}
  @PostMapping("/admin_remove_post") public String removePost(@RequestBody PostRemove b){return console.removePost(b.postId(),b.reason());}
  @PostMapping("/admin_release_privacy") public String releasePrivacy(){return console.releasePrivacy();}
  @PostMapping("/admin_resolve_error") public String resolveError(@RequestBody ErrorId b){return console.resolveError(b.id());}
}
