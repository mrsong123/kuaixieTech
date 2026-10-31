package com.kuaixietech.service.trading.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.trading.DepositsMapper;
import com.kuaixietech.entity.trading.Deposits;
import com.kuaixietech.service.trading.DepositsService;
import org.springframework.stereotype.Service;
@Service
public class DepositsServiceImpl extends ServiceImpl<DepositsMapper,Deposits> implements DepositsService {}
