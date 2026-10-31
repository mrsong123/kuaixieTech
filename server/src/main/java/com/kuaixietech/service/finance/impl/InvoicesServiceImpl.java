package com.kuaixietech.service.finance.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.mapper.finance.InvoicesMapper;
import com.kuaixietech.entity.finance.Invoices;
import com.kuaixietech.service.finance.InvoicesService;
import org.springframework.stereotype.Service;
@Service
public class InvoicesServiceImpl extends ServiceImpl<InvoicesMapper,Invoices> implements InvoicesService {}
