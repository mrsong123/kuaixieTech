package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.CallLogsMapper;
import com.kuaixietech.entity.communication.CallLogs;
import com.kuaixietech.service.communication.CallLogsService;
import org.springframework.stereotype.Service;
@Service
public class CallLogsServiceImpl extends ServiceImpl<CallLogsMapper,CallLogs> implements CallLogsService {}
