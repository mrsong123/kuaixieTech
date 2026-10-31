package com.kuaixietech.service.order_fulfillment.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.order_fulfillment.CancelRequestsMapper;
import com.kuaixietech.entity.order_fulfillment.CancelRequests;
import com.kuaixietech.service.order_fulfillment.CancelRequestsService;
import org.springframework.stereotype.Service;
@Service
public class CancelRequestsServiceImpl extends ServiceImpl<CancelRequestsMapper,CancelRequests> implements CancelRequestsService {}
