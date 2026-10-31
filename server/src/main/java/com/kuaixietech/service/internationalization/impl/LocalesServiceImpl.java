package com.kuaixietech.service.internationalization.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.internationalization.LocalesMapper;
import com.kuaixietech.entity.internationalization.Locales;
import com.kuaixietech.service.internationalization.LocalesService;
import org.springframework.stereotype.Service;
@Service
public class LocalesServiceImpl extends ServiceImpl<LocalesMapper,Locales> implements LocalesService {}
