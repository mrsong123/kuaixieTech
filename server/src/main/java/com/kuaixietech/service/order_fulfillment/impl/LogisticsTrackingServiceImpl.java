package com.kuaixietech.service.order_fulfillment.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.order_fulfillment.LogisticsTracking;
import com.kuaixietech.mapper.order_fulfillment.LogisticsTrackingMapper;
import com.kuaixietech.service.order_fulfillment.LogisticsTrackingService;
import org.springframework.stereotype.Service;
@Service public class LogisticsTrackingServiceImpl extends ServiceImpl<LogisticsTrackingMapper,LogisticsTracking> implements LogisticsTrackingService {}
