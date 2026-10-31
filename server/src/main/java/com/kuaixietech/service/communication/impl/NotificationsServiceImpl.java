package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.NotificationsMapper;
import com.kuaixietech.entity.communication.Notifications;
import com.kuaixietech.service.communication.NotificationsService;
import org.springframework.stereotype.Service;
@Service
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper,Notifications> implements NotificationsService {}
