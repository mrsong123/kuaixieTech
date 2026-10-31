package com.kuaixietech.service.finance.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.finance.CouponsMapper;
import com.kuaixietech.entity.finance.Coupons;
import com.kuaixietech.service.finance.CouponsService;
import org.springframework.stereotype.Service;
@Service
public class CouponsServiceImpl extends ServiceImpl<CouponsMapper,Coupons> implements CouponsService {}
