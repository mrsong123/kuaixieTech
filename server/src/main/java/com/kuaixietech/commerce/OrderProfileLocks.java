package com.kuaixietech.commerce;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
/** Shared stable profile ordering for normal intents and product checkouts. */
@Service
public class OrderProfileLocks {
 private final JdbcTemplate db;
 public OrderProfileLocks(JdbcTemplate db){this.db=db;}
 public void lock(String buyer,String seller){var ids=new TreeSet<String>();ids.add(buyer.toLowerCase(Locale.ROOT));if(seller!=null)ids.add(seller.toLowerCase(Locale.ROOT));for(String user:ids)if(db.queryForList("SELECT id FROM t_profiles WHERE id=? FOR UPDATE",user).isEmpty())throw new IllegalStateException("ORDER_ACCOUNT_UNAVAILABLE");}
 public void forTrip(String buyer,String trip){var rows=db.queryForList("SELECT owner_id FROM t_trips WHERE id=?",trip);lock(buyer,rows.isEmpty()?null:Objects.toString(rows.getFirst().get("owner_id"),null));}
}
