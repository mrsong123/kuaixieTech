package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.JoinApplicationsMapper;
import com.kuaixietech.entity.account.JoinApplications;
import com.kuaixietech.service.account.JoinApplicationsService;
import org.springframework.stereotype.Service;
@Service
public class JoinApplicationsServiceImpl extends ServiceImpl<JoinApplicationsMapper,JoinApplications> implements JoinApplicationsService {}
