package com.kuaixietech.service.growth.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.growth.RewardLedgerMapper;
import com.kuaixietech.entity.growth.RewardLedger;
import com.kuaixietech.service.growth.RewardLedgerService;
import org.springframework.stereotype.Service;
@Service
public class RewardLedgerServiceImpl extends ServiceImpl<RewardLedgerMapper,RewardLedger> implements RewardLedgerService {}
