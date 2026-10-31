package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.VerificationCodesMapper;
import com.kuaixietech.entity.account.VerificationCodes;
import com.kuaixietech.service.account.VerificationCodesService;
import org.springframework.stereotype.Service;
@Service
public class VerificationCodesServiceImpl extends ServiceImpl<VerificationCodesMapper,VerificationCodes> implements VerificationCodesService {}
