package com.kuaixie.market;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LegacySqlProviderTest {
 @Test void bindsValuesAndPreservesQuotedQuestionMarks(){assertEquals("SELECT '?' AS x WHERE id=#{values[0]} AND note=#{values[1]}",LegacySqlProvider.bind("SELECT '?' AS x WHERE id=? AND note=?",2));}
 @Test void rejectsMismatchedParameters(){assertThrows(IllegalArgumentException.class,()->LegacySqlProvider.bind("SELECT ?",0));}
}
