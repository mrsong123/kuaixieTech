package com.kuaixietech.service.order_fulfillment.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.order_fulfillment.DisputesMapper;
import com.kuaixietech.entity.order_fulfillment.Disputes;
import com.kuaixietech.service.order_fulfillment.DisputesService;
import org.springframework.stereotype.Service;
@Service
public class DisputesServiceImpl extends ServiceImpl<DisputesMapper,Disputes> implements DisputesService {}
