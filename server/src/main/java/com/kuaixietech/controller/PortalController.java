package com.kuaixietech.controller;
import com.kuaixietech.service.portal.PortalService;
import com.kuaixietech.entity.portal.PortalDtos;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController public class PortalController {
 private final PortalService svc;
 public PortalController(PortalService svc){this.svc=svc;}
 @GetMapping("/api/public/demands") public List<Map<String,Object>> demands(@RequestParam(defaultValue="30") int limit){return svc.publicDemands(limit);}
 @GetMapping("/api/public/community/feed") public List<Map<String,Object>> feed(@RequestParam(defaultValue="20") int limit){return svc.communityFeed(limit);}
 @GetMapping("/api/portal/me") public Map<String,Object> me(){return svc.verifiedRole();}
 @PostMapping("/api/portal/demands") public Map<String,String> demand(@Valid @RequestBody PortalDtos.DemandCreate b){return svc.createDemand(b);}
 @PostMapping("/api/portal/trips") public Map<String,String> trip(@Valid @RequestBody PortalDtos.TripCreate b){return svc.createTrip(b);}
 @PostMapping("/api/portal/community/posts") public Map<String,String> social(@Valid @RequestBody PortalDtos.SocialPostCreate b){return svc.createSocialPost(b);}
 @GetMapping("/api/portal/admin/reviews") public Map<String,List<Map<String,Object>>> reviews(){return svc.pendingReviews();}
 @PostMapping("/api/portal/admin/trips/review") public void reviewTrip(@Valid @RequestBody PortalDtos.ReviewTrip b){svc.reviewTrip(b);}
 @PostMapping("/api/portal/admin/demands/review") public void reviewDemand(@Valid @RequestBody PortalDtos.ReviewDemand b){svc.reviewDemand(b);}
 @PostMapping("/api/portal/admin/community/review") public void review(@Valid @RequestBody PortalDtos.ReviewPost b){svc.reviewSocialPost(b);}
}
