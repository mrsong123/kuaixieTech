package com.kuaixietech.service.community.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.community.PostComments;
import com.kuaixietech.mapper.community.PostCommentsMapper;
import com.kuaixietech.service.community.PostCommentsService;
import org.springframework.stereotype.Service;
@Service public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper,PostComments> implements PostCommentsService {}
