package com.kuaixietech.service.account.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.account.KycDocumentsMapper;
import com.kuaixietech.entity.account.KycDocuments;
import com.kuaixietech.service.account.KycDocumentsService;
import org.springframework.stereotype.Service;
@Service
public class KycDocumentsServiceImpl extends ServiceImpl<KycDocumentsMapper,KycDocuments> implements KycDocumentsService {}
