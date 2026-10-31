package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.MessagesMapper;
import com.kuaixietech.entity.communication.Messages;
import com.kuaixietech.service.communication.MessagesService;
import org.springframework.stereotype.Service;
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper,Messages> implements MessagesService {}
