package com.kuaixietech.service.portal.impl;
import com.kuaixietech.entity.portal.PortalDtos;
import com.kuaixietech.mapper.portal.PortalMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.portal.PortalService;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service public class PortalServiceImpl implements PortalService {
 private final PortalMapper mapper; private final DomainAccessGuard guard;private final JdbcTemplate jdbc;private final Set<String> mediaHosts;
 public PortalServiceImpl(PortalMapper mapper,DomainAccessGuard guard,JdbcTemplate jdbc,
     @Value("${kuaixie.portal.allowed-media-hosts:}") String hosts){
   this.mapper=mapper;this.guard=guard;this.jdbc=jdbc;
   this.mediaHosts=new HashSet<>();
   if(hosts!=null)for(String host:hosts.split(","))if(!host.isBlank())mediaHosts.add(host.trim().toLowerCase(Locale.ROOT));
 }
 private int limit(int value){if(value<1||value>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"limit must be 1..100");return value;}
 public List<Map<String,Object>> publicDemands(int limit){return mapper.publicDemands(limit(limit));}
 public List<Map<String,Object>> communityFeed(int limit){return mapper.communityFeed(limit(limit));}
 public Map<String,Object> verifiedRole(){String id=guard.requireActiveProfile();Map<String,Object> role=mapper.profileRole(id);
   if(role==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"profile unavailable");return role;}
 @Transactional public Map<String,String> createDemand(PortalDtos.DemandCreate b){
   String user=guard.requireActiveProfile();String id=UUID.randomUUID().toString();
   String type=b.tripType()==null?"any":b.tripType();
   jdbc.update("INSERT INTO t_demands(id,title,item_desc,image_urls,publisher_id,status,visibility,trip_type,city,expect_price,pk_mode) VALUES (?,?,?,JSON_ARRAY(),?,'open','hidden',?,?,?,1)",
      id,b.title().trim(),b.description(),user,type,b.city(),b.expectPrice());
   jdbc.update("INSERT INTO kx_portal_demand_reviews(demand_id,author_id,status) VALUES(?,?,'pending_review')",id,user);
   return Map.of("id",id,"status","pending_review");
 }
 @Transactional public Map<String,String> createTrip(PortalDtos.TripCreate b){
   String user=guard.requireVerifiedCarrier();LocalDate depart;LocalDate returns;
   try{depart=LocalDate.parse(b.departDate());returns=LocalDate.parse(b.returnDate());}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"dates must be ISO yyyy-MM-dd");}
   if(returns.isBefore(depart)||depart.isBefore(LocalDate.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid travel dates");
   String id=UUID.randomUUID().toString();int quantity=b.capacityQty()==null?1:b.capacityQty();
   jdbc.update("""
      INSERT INTO t_trips(id,owner_id,origin_city,destination,transport_mode,depart_date,return_date,capacity_qty,remaining_slots,
      trip_type,direction,status,visibility,verify_status,proof_urls,visa_proof_urls,sms_proof_urls,boarding_risk_flags,product_ids,ticket_proof_urls)
      VALUES(?,?,?,?,?,?,?, ?,?, ?, 'outbound','active','hidden','pending',JSON_ARRAY(),JSON_ARRAY(),JSON_ARRAY(),JSON_ARRAY(),JSON_ARRAY(),JSON_ARRAY())
      """, id,user,b.originCity().trim(),b.destination().trim(),b.transportMode()==null?"other":b.transportMode(),depart,returns,quantity,quantity,b.tripType()==null?"domestic":b.tripType());
   return Map.of("id",id,"status","pending_review");
 }
 private String allowedHttps(String url){
  if(url==null||url.isBlank())return null;
  try{URI uri=URI.create(url);String host=uri.getHost();if(!"https".equalsIgnoreCase(uri.getScheme())||host==null||uri.getUserInfo()!=null||uri.getFragment()!=null||!mediaHosts.contains(host.toLowerCase(Locale.ROOT)))throw new IllegalArgumentException();return url;}
  catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"media must be an HTTPS URL on the configured upload host");}
 }
 @Transactional public Map<String,String> createSocialPost(PortalDtos.SocialPostCreate b){
  String user=guard.requireActiveProfile();String kind=b.mediaKind()==null?"text":b.mediaKind();
  String url=allowedHttps(b.mediaUrl());String poster=allowedHttps(b.posterUrl());
  if(!kind.equals("text")&&url==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"media URL required");
  if(kind.equals("text")&&url!=null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"text posts cannot contain media URL");
  String id=UUID.randomUUID().toString();jdbc.update("INSERT INTO kx_social_posts(id,author_id,caption,media_kind,media_url,poster_url,status) VALUES(?,?,?,?,?,?,'pending_review')",id,user,b.caption().trim(),kind,url,poster);
  return Map.of("id",id,"status","pending_review");
 }
 public Map<String,List<Map<String,Object>>> pendingReviews(){guard.requireAdmin();return Map.of("demands",mapper.pendingDemands(),"trips",mapper.pendingTrips(),"posts",mapper.pendingPosts());}
 @Transactional public void reviewTrip(PortalDtos.ReviewTrip b){
  guard.requireAdmin();
  int n=jdbc.update("UPDATE t_trips SET verify_status=?,visibility=?,verified_at=IF(?=1,NOW(),NULL) WHERE id=? AND verify_status='pending'",b.approved()?"approved":"rejected",b.approved()?"public":"hidden",b.approved()?1:0,b.tripId());
  if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"trip not awaiting review");
 }
 @Transactional public void reviewDemand(PortalDtos.ReviewDemand b){
  String admin=guard.requireAdmin();
  int n=jdbc.update("UPDATE kx_portal_demand_reviews SET status=?,reviewer_id=?,reviewed_at=NOW() WHERE demand_id=? AND status='pending_review'",b.approved()?"approved":"rejected",admin,b.demandId());
  if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"request not awaiting review");
  if(b.approved())jdbc.update("UPDATE t_demands SET visibility='public' WHERE id=? AND status='open'",b.demandId());
 }
 @Transactional public void reviewSocialPost(PortalDtos.ReviewPost b){
  String user=guard.requireAdmin();int n=jdbc.update("UPDATE kx_social_posts SET status=?,reviewer_id=?,reviewed_at=NOW() WHERE id=? AND status='pending_review'",b.approved()?"approved":"rejected",user,b.postId());
  if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"post not awaiting review");
 }
}
