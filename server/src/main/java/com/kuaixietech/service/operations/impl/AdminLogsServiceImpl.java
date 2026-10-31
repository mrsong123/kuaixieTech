package com.kuaixietech.service.operations.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.operations.AdminLogsMapper;
import com.kuaixietech.entity.operations.AdminLogs;
import com.kuaixietech.service.operations.AdminLogsService;
import org.springframework.stereotype.Service;
@Service
public class AdminLogsServiceImpl extends ServiceImpl<AdminLogsMapper,AdminLogs> implements AdminLogsService {}
