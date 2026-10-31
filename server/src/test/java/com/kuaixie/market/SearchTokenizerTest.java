package com.kuaixie.market;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SearchTokenizerTest {
 private final SearchTokenizer t=new SearchTokenizer();
 @Test void hanPinyinAndInitials(){assertTrue(t.matches("美白护肤 skincare","护肤"));assertTrue(t.matches("美白护肤 skincare","hufu"));assertTrue(t.matches("美白护肤 skincare","mbhf"));assertTrue(t.matches("SKINCARE","skincare"));assertTrue(t.matches("ＳＫＩＮＣＡＲＥ","skincare"));assertFalse(t.matches("面膜","hufu"));}
 @Test void allWordsMustMatch(){assertTrue(t.matches("日本护肤 Serum","hufu serum"));assertFalse(t.matches("日本护肤 Serum","hufu missing"));}
}
