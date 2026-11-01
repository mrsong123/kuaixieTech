package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.controller.AdminConsoleRpcController;
import com.kuaixietech.mapper.operations.AdminConsoleMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.operations.impl.AdminConsoleService;
import java.util.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AdminConsolePagingTest {
 static final List<String> KINDS=List.of("carrierKyc","buyerKyc","enterpriseKyc","trips","proofRecipes","products","categories","errorLogs","orders","cancels","disputes","invoices","privacyPool","pools","demands","notifyQueue","withdrawals","community","users","finance","invite","broadcast","channelConfigs","locales","adminLogs");
 final AdminConsoleMapper mapper=mock(AdminConsoleMapper.class);
 final DomainAccessGuard guard=mock(DomainAccessGuard.class);
 final AdminConsoleService service=new AdminConsoleService(mapper,guard,null,null,null,null);
 @Test void olderCallersAndBodyDefaultToFirstPage(){service.list("orders",null,null);verify(mapper).orders(80,0);assertNull(new AdminConsoleRpcController.ListBody("orders",80,null).offset());}
 @Test void allTwentyFiveKindsPassServerOffset(){for(String kind:KINDS)service.list(kind,81,null,160);var calls=mockingDetails(mapper).getInvocations();assertEquals(25,calls.size());for(var call:calls){assertTrue(KINDS.contains(call.getMethod().getName()));assertEquals(Integer.valueOf(81),call.getArgument(0));assertEquals(Integer.valueOf(160),call.getArgument(1));}}
 @Test void userFilterAndLimitBoundArePreserved(){service.list("users",500,"  alice  ",80);verify(mapper).users(200,80,"alice");}
 @Test void negativeOffsetAndUnknownKindCannotReachSql(){assertEquals(400,assertThrows(ResponseStatusException.class,()->service.list("orders",80,null,-1)).getStatusCode().value());assertEquals(400,assertThrows(ResponseStatusException.class,()->service.list("not_a_list",80,null,0)).getStatusCode().value());verifyNoInteractions(mapper);}
 @Test void nonAdministratorCannotUsePaginationToReadRows(){when(guard.requireAdmin()).thenThrow(new SecurityException());assertThrows(SecurityException.class,()->service.list("finance",80,null,160));verifyNoInteractions(mapper);}
 @Test void eachMappedQueryBindsOffsetAndUniqueStableOrder()throws Exception{var configuration=new Configuration();try(var in=getClass().getResourceAsStream("/mapper/operations/AdminConsoleMapper.xml")){assertNotNull(in);new XMLMapperBuilder(in,configuration,"mapper/operations/AdminConsoleMapper.xml",configuration.getSqlFragments()).parse();}for(String kind:KINDS){var params=new HashMap<String,Object>();params.put("limit",81);params.put("offset",160);params.put("keyword",null);var sql=configuration.getMappedStatement("com.kuaixietech.mapper.operations.AdminConsoleMapper."+kind).getBoundSql(params);String normalized=sql.getSql().replaceAll("\\s+"," ").trim();assertTrue(normalized.endsWith("LIMIT ? OFFSET ?"),kind+": "+normalized);assertTrue(sql.getParameterMappings().stream().anyMatch(p->p.getProperty().equals("offset")),kind);String order=normalized.substring(normalized.lastIndexOf("ORDER BY"));String key=kind.equals("privacyPool")?"virtual_number":kind.equals("locales")||kind.equals("invite")?"code":"id";assertTrue(order.matches(".*\\b"+key+"(?: DESC)? LIMIT \\? OFFSET \\?"),kind+": "+order);}}
}
