package com.kuaixietech.service.finance.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.finance.PayOrdersMapper;
import com.kuaixietech.entity.finance.PayOrders;
import com.kuaixietech.service.finance.PayOrdersService;
import org.springframework.stereotype.Service;
@Service
public class PayOrdersServiceImpl extends ServiceImpl<PayOrdersMapper,PayOrders> implements PayOrdersService {}
