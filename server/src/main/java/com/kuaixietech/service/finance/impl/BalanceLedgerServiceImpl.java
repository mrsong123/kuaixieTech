package com.kuaixietech.service.finance.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.finance.BalanceLedgerMapper;
import com.kuaixietech.entity.finance.BalanceLedger;
import com.kuaixietech.service.finance.BalanceLedgerService;
import org.springframework.stereotype.Service;
@Service
public class BalanceLedgerServiceImpl extends ServiceImpl<BalanceLedgerMapper,BalanceLedger> implements BalanceLedgerService {}
