package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.FaceVerificationsMapper;
import com.kuaixietech.entity.account.FaceVerifications;
import com.kuaixietech.service.account.FaceVerificationsService;
import org.springframework.stereotype.Service;
@Service
public class FaceVerificationsServiceImpl extends ServiceImpl<FaceVerificationsMapper,FaceVerifications> implements FaceVerificationsService {}
