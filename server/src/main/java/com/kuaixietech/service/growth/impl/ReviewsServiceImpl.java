package com.kuaixietech.service.growth.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.growth.ReviewsMapper;
import com.kuaixietech.entity.growth.Reviews;
import com.kuaixietech.service.growth.ReviewsService;
import org.springframework.stereotype.Service;
@Service
public class ReviewsServiceImpl extends ServiceImpl<ReviewsMapper,Reviews> implements ReviewsService {}
