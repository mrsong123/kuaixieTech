package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.NotifyOutboxMapper;
import com.kuaixietech.entity.communication.NotifyOutbox;
import com.kuaixietech.service.communication.NotifyOutboxService;
import org.springframework.stereotype.Service;
@Service
public class NotifyOutboxServiceImpl extends ServiceImpl<NotifyOutboxMapper,NotifyOutbox> implements NotifyOutboxService {}
