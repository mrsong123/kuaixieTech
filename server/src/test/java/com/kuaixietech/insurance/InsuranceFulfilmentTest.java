package com.kuaixietech.insurance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.order_fulfillment.OrderProofMigrationMapper;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.order_fulfillment.impl.OrderProofMigrationService;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
class InsuranceFulfilmentTest {
 String carrier="822de014-ae8d-485f-bd06-cd972ac00970";OrderProofMigrationMapper db;OrderProofMigrationService service;
 @BeforeEach void init(){SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(carrier,"unit",List.of()));db=mock(OrderProofMigrationMapper.class);service=new OrderProofMigrationService(db,mock(AtomicMoneyService.class),new ObjectMapper());}
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void selectedButUnissuedInsuranceBlocksPurchaseAndShipment(){var order=new HashMap<String,Object>(Map.of("vendor_id",carrier,"status","locked","agreed_json","{\"pricing_version\":\"escrow_v2_external_insurance\",\"insurance_choice\":\"yes\"}"));when(db.lockOrder("order")).thenReturn(order);assertEquals("ERR:INSURANCE_NOT_EFFECTIVE",service.confirmPurchase("order",null,null,null,null,null,null));order.put("status","purchased");assertEquals("ERR:INSURANCE_NOT_EFFECTIVE",service.confirmShip("order","Carrier","tracking",null,null,null,null,null));verify(db,never()).savePurchase(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),anyBoolean());verify(db,never()).saveShipment(any(),any(),any(),any(),any(),any(),any(),any(),any());}
 @Test void waiverDoesNotRequireAnyPolicyOrInventCoverage(){when(db.lockOrder("order")).thenReturn(Map.of("vendor_id",carrier,"status","wrong_status","agreed_json","{\"pricing_version\":\"escrow_v2_external_insurance\",\"insurance_choice\":\"waived\"}"));assertEquals("ERR:BAD_STATUS",service.confirmPurchase("order",null,null,null,null,null,null));verify(db,never()).activeGoodsInsurance(any());}
}
