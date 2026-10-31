package com.kuaixietech.mapper.growth;
import org.apache.ibatis.annotations.Mapper;
@Mapper public interface LegacyCounterMigrationMapper { int reset90DayCounters(); }
