package com.kuaixietech.service.platform.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.platform.Announcements;
import com.kuaixietech.mapper.platform.AnnouncementsMapper;
import com.kuaixietech.service.platform.AnnouncementsService;
import org.springframework.stereotype.Service;
@Service public class AnnouncementsServiceImpl extends ServiceImpl<AnnouncementsMapper,Announcements> implements AnnouncementsService {}
