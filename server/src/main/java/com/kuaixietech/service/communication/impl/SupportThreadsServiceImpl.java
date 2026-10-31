package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.SupportThreadsMapper;
import com.kuaixietech.entity.communication.SupportThreads;
import com.kuaixietech.service.communication.SupportThreadsService;
import org.springframework.stereotype.Service;
@Service
public class SupportThreadsServiceImpl extends ServiceImpl<SupportThreadsMapper,SupportThreads> implements SupportThreadsService {}
