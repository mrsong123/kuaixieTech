package com.kuaixietech.service.assist.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.assist.AssistRequests;
import com.kuaixietech.mapper.assist.AssistRequestsMapper;
import com.kuaixietech.service.assist.AssistRequestsService;
import org.springframework.stereotype.Service;
@Service public class AssistRequestsServiceImpl extends ServiceImpl<AssistRequestsMapper,AssistRequests> implements AssistRequestsService {}
