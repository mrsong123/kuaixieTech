package com.kuaixie.market;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
class WorkflowTest {
 @Test void closedTicketIsTerminal(){for(String s:new String[]{"open","in_progress","resolved","closed"})assertFalse(SupportApi.allowed("closed",s));assertTrue(SupportApi.allowed("open","resolved"));assertTrue(SupportApi.allowed("resolved","closed"));assertFalse(SupportApi.allowed("in_progress","closed"));}
 @Test void pageBoundsAvoidOverflow(){assertThrows(ResponseStatusException.class,()->QueryPage.validate(Integer.MAX_VALUE,100));assertThrows(ResponseStatusException.class,()->QueryPage.validate(0,0));assertDoesNotThrow(()->QueryPage.validate(100000,100));}
}
