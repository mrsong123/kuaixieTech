package com.kuaixietech.service.trip.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.trip.TripCategoriesMapper;
import com.kuaixietech.entity.trip.TripCategories;
import com.kuaixietech.service.trip.TripCategoriesService;
import org.springframework.stereotype.Service;
@Service
public class TripCategoriesServiceImpl extends ServiceImpl<TripCategoriesMapper,TripCategories> implements TripCategoriesService {}
