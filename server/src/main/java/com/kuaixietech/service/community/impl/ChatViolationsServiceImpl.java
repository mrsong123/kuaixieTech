package com.kuaixietech.service.community.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.community.ChatViolationsMapper;
import com.kuaixietech.entity.community.ChatViolations;
import com.kuaixietech.service.community.ChatViolationsService;
import org.springframework.stereotype.Service;
@Service
public class ChatViolationsServiceImpl extends ServiceImpl<ChatViolationsMapper,ChatViolations> implements ChatViolationsService {}
