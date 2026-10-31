package com.kuaixietech.service.catalog.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.catalog.ProductsMapper;
import com.kuaixietech.entity.catalog.Products;
import com.kuaixietech.service.catalog.ProductsService;
import org.springframework.stereotype.Service;
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper,Products> implements ProductsService {}
