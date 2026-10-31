package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.SupportMessagesMapper;
import com.kuaixietech.entity.communication.SupportMessages;
import com.kuaixietech.service.communication.SupportMessagesService;
import org.springframework.stereotype.Service;
@Service
public class SupportMessagesServiceImpl extends ServiceImpl<SupportMessagesMapper,SupportMessages> implements SupportMessagesService {}
