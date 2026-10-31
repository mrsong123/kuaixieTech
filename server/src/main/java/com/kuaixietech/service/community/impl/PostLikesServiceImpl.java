package com.kuaixietech.service.community.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.community.PostLikes;
import com.kuaixietech.mapper.community.PostLikesMapper;
import com.kuaixietech.service.community.PostLikesService;
import org.springframework.stereotype.Service;
@Service public class PostLikesServiceImpl extends ServiceImpl<PostLikesMapper,PostLikes> implements PostLikesService {}
