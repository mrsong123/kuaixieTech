package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.AddressesMapper;
import com.kuaixietech.entity.account.Addresses;
import com.kuaixietech.service.account.AddressesService;
import org.springframework.stereotype.Service;
@Service
public class AddressesServiceImpl extends ServiceImpl<AddressesMapper,Addresses> implements AddressesService {}
