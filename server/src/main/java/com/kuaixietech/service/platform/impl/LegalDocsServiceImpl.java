package com.kuaixietech.service.platform.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.platform.LegalDocsMapper;
import com.kuaixietech.entity.platform.LegalDocs;
import com.kuaixietech.service.platform.LegalDocsService;
import org.springframework.stereotype.Service;
@Service
public class LegalDocsServiceImpl extends ServiceImpl<LegalDocsMapper,LegalDocs> implements LegalDocsService {}
