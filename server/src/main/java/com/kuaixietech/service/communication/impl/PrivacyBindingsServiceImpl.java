package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.PrivacyBindingsMapper;
import com.kuaixietech.entity.communication.PrivacyBindings;
import com.kuaixietech.service.communication.PrivacyBindingsService;
import org.springframework.stereotype.Service;
@Service
public class PrivacyBindingsServiceImpl extends ServiceImpl<PrivacyBindingsMapper,PrivacyBindings> implements PrivacyBindingsService {}
