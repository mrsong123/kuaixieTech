package com.kuaixie.market;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import org.junit.jupiter.api.*;

/** Real TCP/authentication/database paging checks; support fixture is not a populated upgrade test. */
class SupportPaginationHttpTest {
  FinanceHttpTest h;
  @BeforeEach void setup() throws Exception {
    h=new FinanceHttpTest();h.setup();
    var db=h.fixture.db;
    db.update("ALTER TABLE users ADD nickname VARCHAR(80)");
    db.update("UPDATE users SET nickname=id");
    db.update("DROP TABLE IF EXISTS support_ticket_messages");
    db.update("DROP TABLE IF EXISTS support_tickets");
    db.update("CREATE TABLE support_tickets(id VARCHAR(80) PRIMARY KEY,user_id VARCHAR(80),subject VARCHAR(160),body VARCHAR(4000),category VARCHAR(20),order_id VARCHAR(80),status VARCHAR(20),revision INT,created_at TIMESTAMP(6),updated_at TIMESTAMP(6))");
    db.update("CREATE TABLE support_ticket_messages(id VARCHAR(80) PRIMARY KEY,ticket_id VARCHAR(80),author_id VARCHAR(80),body VARCHAR(4000),message_type VARCHAR(16),created_at TIMESTAMP(6),INDEX(ticket_id,created_at))");
    for(String t:List.of("t","other"))db.update("INSERT INTO support_tickets VALUES(?,'buyer','subject','body','other',NULL,'open',0,NOW(),NOW())",t);
    for(int i=0;i<205;i++)add(String.format("m%04d",i),"t","2026-01-01 00:00:00.123456");
    add("foreign","other","2026-01-01 00:00:00.123456");
  }
  void add(String id,String ticket,String time){h.fixture.db.update("INSERT INTO support_ticket_messages VALUES(?,?,'buyer','body','reply',?)",id,ticket,java.sql.Timestamp.valueOf(time));}
  @AfterEach void close(){if(h!=null)h.close();}
  JsonNode get(String query,String who,int status)throws Exception{
    var r=h.send("GET","/support/tickets/t"+query,who,null,null);assertEquals(status,r.statusCode(),r.body());return h.json.readTree(r.body());
  }
  @Test void moreThan200SameTimestampTraversesWithoutLossOrDuplicates()throws Exception{
    var first=get("","buyer",200);assertEquals(200,first.path("messages").size());assertTrue(first.path("hasMore").asBoolean());
    Set<String> seen=new HashSet<>();first.path("messages").forEach(m->assertTrue(seen.add(m.path("id").asText())));
    var older=get("?before="+first.path("nextBefore").asText(),"buyer",200);assertEquals(5,older.path("messages").size());assertFalse(older.path("hasMore").asBoolean());
    older.path("messages").forEach(m->assertTrue(seen.add(m.path("id").asText())));assertEquals(205,seen.size());assertEquals("m0000",older.path("messages").get(0).path("id").asText());
    assertEquals(0,h.fixture.db.queryForObject("SELECT revision FROM support_tickets WHERE id='t'",Integer.class));
  }
  @Test void newReplyDoesNotShiftOlderPageAndCursorReplayIsStable()throws Exception{
    var first=get("?size=100","buyer",200);String query="?size=100&before="+first.path("nextBefore").asText();var old=get(query,"buyer",200);
    add("newer","t","2026-02-01 00:00:00");assertEquals(old.path("messages"),get(query,"buyer",200).path("messages"));
    assertEquals("m0005",old.path("messages").get(0).path("id").asText());
  }
  @Test void permissionsInvalidSizesAndForeignCursorsAreRejected()throws Exception{
    get("?before=foreign","buyer",400);get("?before=missing","buyer",400);get("?size=0","buyer",400);get("?size=201","buyer",400);
    get("","outsider",403);get("",null,401);assertEquals(1,get("?size=1","admin",200).path("messages").size());
  }
}
