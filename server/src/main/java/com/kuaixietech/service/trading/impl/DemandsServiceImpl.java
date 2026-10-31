package com.kuaixietech.service.trading.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.trading.DemandsMapper;
import com.kuaixietech.entity.trading.Demands;
import com.kuaixietech.service.trading.DemandsService;
import org.springframework.stereotype.Service;
@Service
public class DemandsServiceImpl extends ServiceImpl<DemandsMapper,Demands> implements DemandsService {}
