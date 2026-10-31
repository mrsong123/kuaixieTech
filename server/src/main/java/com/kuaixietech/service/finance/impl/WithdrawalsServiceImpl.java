package com.kuaixietech.service.finance.impl;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kuaixietech.entity.finance.Withdrawals;
import com.kuaixietech.mapper.finance.WithdrawalsMapper;
import com.kuaixietech.service.finance.WithdrawalsService;
import org.springframework.stereotype.Service;
@Service public class WithdrawalsServiceImpl extends ServiceImpl<WithdrawalsMapper,Withdrawals> implements WithdrawalsService {}
