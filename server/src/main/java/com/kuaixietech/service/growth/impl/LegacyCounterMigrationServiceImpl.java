package com.kuaixietech.service.growth.impl;
import com.kuaixietech.mapper.growth.LegacyCounterMigrationMapper;
import com.kuaixietech.service.growth.LegacyCounterMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class LegacyCounterMigrationServiceImpl implements LegacyCounterMigrationService {
 private final LegacyCounterMigrationMapper mapper;
 public LegacyCounterMigrationServiceImpl(LegacyCounterMigrationMapper mapper){this.mapper=mapper;}
 @Override @Transactional public void reset90DayCounters(){mapper.reset90DayCounters();}
}
