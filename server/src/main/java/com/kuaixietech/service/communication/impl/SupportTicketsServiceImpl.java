package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.SupportTicketsMapper;
import com.kuaixietech.entity.communication.SupportTickets;
import com.kuaixietech.service.communication.SupportTicketsService;
import org.springframework.stereotype.Service;
@Service
public class SupportTicketsServiceImpl extends ServiceImpl<SupportTicketsMapper,SupportTickets> implements SupportTicketsService {}
