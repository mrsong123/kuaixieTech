package com.kuaixietech.service.growth.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.growth.ScoreLogsMapper;
import com.kuaixietech.entity.growth.ScoreLogs;
import com.kuaixietech.service.growth.ScoreLogsService;
import org.springframework.stereotype.Service;
@Service
public class ScoreLogsServiceImpl extends ServiceImpl<ScoreLogsMapper,ScoreLogs> implements ScoreLogsService {}
