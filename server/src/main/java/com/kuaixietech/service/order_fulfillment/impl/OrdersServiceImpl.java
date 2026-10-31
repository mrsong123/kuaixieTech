package com.kuaixietech.service.order_fulfillment.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.order_fulfillment.OrdersMapper;
import com.kuaixietech.entity.order_fulfillment.Orders;
import com.kuaixietech.service.order_fulfillment.OrdersService;
import org.springframework.stereotype.Service;
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper,Orders> implements OrdersService {}
