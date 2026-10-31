package com.kuaixietech.service.trading.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.trading.QuotesMapper;
import com.kuaixietech.entity.trading.Quotes;
import com.kuaixietech.service.trading.QuotesService;
import org.springframework.stereotype.Service;
@Service
public class QuotesServiceImpl extends ServiceImpl<QuotesMapper,Quotes> implements QuotesService {}
