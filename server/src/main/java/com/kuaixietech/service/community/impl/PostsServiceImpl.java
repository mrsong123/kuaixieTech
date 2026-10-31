package com.kuaixietech.service.community.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.community.Posts;
import com.kuaixietech.mapper.community.PostsMapper;
import com.kuaixietech.service.community.PostsService;
import org.springframework.stereotype.Service;
@Service public class PostsServiceImpl extends ServiceImpl<PostsMapper,Posts> implements PostsService {}
