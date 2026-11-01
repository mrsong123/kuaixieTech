package com.kuaixietech.service.account.impl;

import com.kuaixietech.mapper.account.AccountCentreMapper;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Personal projections always derive identity from the verified session, including for admins. */
@Service
@Transactional(readOnly=true)
public class AccountCentreService {
  private final AccountCentreMapper db;
  private final DomainAccessGuard guard;
  public AccountCentreService(AccountCentreMapper db,DomainAccessGuard guard){this.db=db;this.guard=guard;}
  private void page(int limit,int offset){
    if(limit<1||limit>100||offset<0||offset>1_000_000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid pagination");
  }
  private Map<String,Object> found(Map<String,Object> row){
    if(row==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"record unavailable");
    return row;
  }
  public Map<String,Object> profile(){return found(db.profile(guard.requireActiveProfile()));}
  @Transactional
  public Map<String,Object> updateProfile(String nickname,String city){
    String user=guard.requireActiveProfile();
    if(nickname==null&&city==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"no profile fields provided");
    if(nickname!=null&&(nickname.isBlank()||nickname.trim().length()>64)||city!=null&&city.trim().length()>64)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid profile fields");
    db.updateProfile(user,nickname==null?null:nickname.trim(),city==null?null:city.trim());
    return found(db.profile(user));
  }
  public List<Map<String,Object>> orders(String role,int limit,int offset){
    String user=guard.requireActiveProfile();page(limit,offset);
    if(!Set.of("all","buyer","carrier").contains(role))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid order role");
    return db.orders(user,role,limit,offset);
  }
  public Map<String,Object> order(String id){return found(db.order(guard.requireActiveProfile(),id));}
  public List<Map<String,Object>> demands(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.demands(user,limit,offset);}
  public List<Map<String,Object>> trips(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.trips(user,limit,offset);}
  public Map<String,Object> wallet(){return found(db.wallet(guard.requireActiveProfile()));}
  public List<Map<String,Object>> ledger(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.ledger(user,limit,offset);}
  public List<Map<String,Object>> notifications(int limit,int offset){String user=guard.requireActiveProfile();page(limit,offset);return db.notifications(user,limit,offset);}
  @Transactional
  public Map<String,String> readNotification(String id){
    String user=guard.requireActiveProfile();
    if(db.readNotification(user,id)==0&&db.ownsNotification(user,id)==0)
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,"notification unavailable");
    return Map.of("status","OK");
  }
}
