package com.kuaixietech.payments;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.time.*;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static com.kuaixietech.payments.PaymentModels.*;

class FxServiceTest {
  @Test void staleRatesCannotBeTurnedIntoTransactionQuotes(){
    var store=mock(PaymentStore.class);var http=mock(PaymentHttp.class);
    when(store.sources()).thenReturn(List.of(new FxSource("source","test fixture","open_er_api",null,null,3600,true,true)));
    when(http.endpoint(anyString(),anyBoolean())).thenReturn(URI.create("https://rates.example.test"));
    when(http.send(any())).thenReturn("{\"result\":\"success\",\"base_code\":\"CNY\",\"time_last_update_unix\":1,\"time_next_update_unix\":2,\"rates\":{\"USD\":0.14}}");
    assertThrows(IllegalStateException.class,()->new FxService(store,http).rate("USD",true));
  }
  @Test void referenceOnlySourceCannotPriceARealPayment(){
    var store=mock(PaymentStore.class);var http=mock(PaymentHttp.class);
    when(store.sources()).thenReturn(List.of(new FxSource("source","test fixture","open_er_api",null,null,3600,true,false)));
    assertThrows(IllegalStateException.class,()->new FxService(store,http).rate("USD",true));verifyNoInteractions(http);
  }
  @Test void freshRatesAreExactAndCachedWithoutExtendingTheirExpiry(){
    var store=mock(PaymentStore.class);var http=mock(PaymentHttp.class);long now=Instant.now().getEpochSecond();
    when(store.sources()).thenReturn(List.of(new FxSource("source","test fixture","open_er_api",null,null,3600,true,true)));
    when(http.endpoint(anyString(),anyBoolean())).thenReturn(URI.create("https://rates.example.test"));
    when(http.send(any())).thenReturn("{\"result\":\"success\",\"base_code\":\"CNY\",\"time_last_update_unix\":"+(now-30)+",\"time_next_update_unix\":"+(now+300)+",\"rates\":{\"USD\":0.140123456789123456}}");
    var service=new FxService(store,http);Rate first=service.rate("USD",true),second=service.rate("USD",true);
    assertEquals(new BigDecimal("0.140123456789123456"),first.value());assertEquals(first,second);verify(http,times(1)).send(any());
  }
  @Test void cnyIdentityNeedsNoMadeUpExternalRate(){
    var store=mock(PaymentStore.class);var http=mock(PaymentHttp.class);
    assertEquals(BigDecimal.ONE,new FxService(store,http).rate("CNY",true).value());verifyNoInteractions(store,http);
  }
}
