package com.kuaixietech.service.order_fulfillment.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.order_fulfillment.OrderLogisticsMapper;
import com.kuaixietech.entity.order_fulfillment.OrderLogistics;
import com.kuaixietech.service.order_fulfillment.OrderLogisticsService;
import org.springframework.stereotype.Service;
@Service
public class OrderLogisticsServiceImpl extends ServiceImpl<OrderLogisticsMapper,OrderLogistics> implements OrderLogisticsService {}
