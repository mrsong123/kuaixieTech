package com.kuaixietech.service.catalog.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.catalog.CartItemsMapper;
import com.kuaixietech.entity.catalog.CartItems;
import com.kuaixietech.service.catalog.CartItemsService;
import org.springframework.stereotype.Service;
@Service
public class CartItemsServiceImpl extends ServiceImpl<CartItemsMapper,CartItems> implements CartItemsService {}
