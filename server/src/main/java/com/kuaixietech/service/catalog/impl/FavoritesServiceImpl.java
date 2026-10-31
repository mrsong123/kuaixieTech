package com.kuaixietech.service.catalog.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.catalog.FavoritesMapper;
import com.kuaixietech.entity.catalog.Favorites;
import com.kuaixietech.service.catalog.FavoritesService;
import org.springframework.stereotype.Service;
@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper,Favorites> implements FavoritesService {}
