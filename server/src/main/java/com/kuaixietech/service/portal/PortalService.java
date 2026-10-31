package com.kuaixietech.service.portal;
import com.kuaixietech.entity.portal.PortalDtos;
import java.util.List;
import java.util.Map;
public interface PortalService {
 List<Map<String,Object>> publicDemands(int limit);
 List<Map<String,Object>> communityFeed(int limit);
 Map<String,Object> verifiedRole();
 Map<String,String> createDemand(PortalDtos.DemandCreate body);
 Map<String,String> createTrip(PortalDtos.TripCreate body);
 Map<String,String> createSocialPost(PortalDtos.SocialPostCreate body);
 void reviewSocialPost(PortalDtos.ReviewPost body);
 void reviewDemand(PortalDtos.ReviewDemand body);
 void reviewTrip(PortalDtos.ReviewTrip body);
 Map<String,List<Map<String,Object>>> pendingReviews();
}
