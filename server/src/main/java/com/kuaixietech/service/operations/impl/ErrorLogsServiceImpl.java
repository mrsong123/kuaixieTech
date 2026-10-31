package com.kuaixietech.service.operations.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.operations.ErrorLogs;
import com.kuaixietech.mapper.operations.ErrorLogsMapper;
import com.kuaixietech.service.operations.ErrorLogsService;
import org.springframework.stereotype.Service;
@Service public class ErrorLogsServiceImpl extends ServiceImpl<ErrorLogsMapper,ErrorLogs> implements ErrorLogsService {}
