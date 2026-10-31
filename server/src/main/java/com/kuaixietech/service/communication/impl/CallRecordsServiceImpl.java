package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.CallRecordsMapper;
import com.kuaixietech.entity.communication.CallRecords;
import com.kuaixietech.service.communication.CallRecordsService;
import org.springframework.stereotype.Service;
@Service
public class CallRecordsServiceImpl extends ServiceImpl<CallRecordsMapper,CallRecords> implements CallRecordsService {}
