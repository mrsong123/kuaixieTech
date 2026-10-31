package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.ProfilesMapper;
import com.kuaixietech.entity.account.Profiles;
import com.kuaixietech.service.account.ProfilesService;
import org.springframework.stereotype.Service;
@Service
public class ProfilesServiceImpl extends ServiceImpl<ProfilesMapper,Profiles> implements ProfilesService {}
