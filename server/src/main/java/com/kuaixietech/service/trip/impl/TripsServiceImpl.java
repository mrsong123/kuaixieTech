package com.kuaixietech.service.trip.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.trip.TripsMapper;
import com.kuaixietech.entity.trip.Trips;
import com.kuaixietech.service.trip.TripsService;
import org.springframework.stereotype.Service;
@Service
public class TripsServiceImpl extends ServiceImpl<TripsMapper,Trips> implements TripsService {}
