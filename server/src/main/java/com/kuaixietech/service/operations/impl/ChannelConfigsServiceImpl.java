package com.kuaixietech.service.operations.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.operations.ChannelConfigs;
import com.kuaixietech.mapper.operations.ChannelConfigsMapper;
import com.kuaixietech.service.operations.ChannelConfigsService;
import org.springframework.stereotype.Service;
@Service public class ChannelConfigsServiceImpl extends ServiceImpl<ChannelConfigsMapper,ChannelConfigs> implements ChannelConfigsService {}
