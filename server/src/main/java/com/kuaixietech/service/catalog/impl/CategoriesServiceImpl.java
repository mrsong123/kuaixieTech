package com.kuaixietech.service.catalog.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.catalog.CategoriesMapper;
import com.kuaixietech.entity.catalog.Categories;
import com.kuaixietech.service.catalog.CategoriesService;
import org.springframework.stereotype.Service;
@Service
public class CategoriesServiceImpl extends ServiceImpl<CategoriesMapper,Categories> implements CategoriesService {}
