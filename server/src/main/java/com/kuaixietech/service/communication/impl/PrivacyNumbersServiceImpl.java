package com.kuaixietech.service.communication.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.communication.PrivacyNumbersMapper;
import com.kuaixietech.entity.communication.PrivacyNumbers;
import com.kuaixietech.service.communication.PrivacyNumbersService;
import org.springframework.stereotype.Service;
@Service
public class PrivacyNumbersServiceImpl extends ServiceImpl<PrivacyNumbersMapper,PrivacyNumbers> implements PrivacyNumbersService {}
