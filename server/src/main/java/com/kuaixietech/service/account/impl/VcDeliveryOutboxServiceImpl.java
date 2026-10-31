package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.VcDeliveryOutboxMapper;
import com.kuaixietech.entity.account.VcDeliveryOutbox;
import com.kuaixietech.service.account.VcDeliveryOutboxService;
import org.springframework.stereotype.Service;
@Service
public class VcDeliveryOutboxServiceImpl extends ServiceImpl<VcDeliveryOutboxMapper,VcDeliveryOutbox> implements VcDeliveryOutboxService {}
