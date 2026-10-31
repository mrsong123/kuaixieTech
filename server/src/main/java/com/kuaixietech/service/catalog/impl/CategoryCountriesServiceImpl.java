package com.kuaixietech.service.catalog.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.catalog.CategoryCountriesMapper;
import com.kuaixietech.entity.catalog.CategoryCountries;
import com.kuaixietech.service.catalog.CategoryCountriesService;
import org.springframework.stereotype.Service;
@Service
public class CategoryCountriesServiceImpl extends ServiceImpl<CategoryCountriesMapper,CategoryCountries> implements CategoryCountriesService {}
