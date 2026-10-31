package com.kuaixietech.service.trading.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.trading.PublishDepositsMapper;
import com.kuaixietech.entity.trading.PublishDeposits;
import com.kuaixietech.service.trading.PublishDepositsService;
import org.springframework.stereotype.Service;
@Service
public class PublishDepositsServiceImpl extends ServiceImpl<PublishDepositsMapper,PublishDeposits> implements PublishDepositsService {}
