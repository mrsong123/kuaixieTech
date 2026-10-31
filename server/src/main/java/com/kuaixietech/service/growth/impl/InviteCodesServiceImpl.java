package com.kuaixietech.service.growth.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.growth.InviteCodesMapper;
import com.kuaixietech.entity.growth.InviteCodes;
import com.kuaixietech.service.growth.InviteCodesService;
import org.springframework.stereotype.Service;
@Service
public class InviteCodesServiceImpl extends ServiceImpl<InviteCodesMapper,InviteCodes> implements InviteCodesService {}
