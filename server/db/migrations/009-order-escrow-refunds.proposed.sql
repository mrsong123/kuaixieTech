-- Deployment proposal only: proven goods-escrow wallet refunds, never original payment-channel refunds.
-- Does not backfill legacy orders, move balances, invent insurance protection or change fee policy.
CREATE TABLE IF NOT EXISTS t_order_escrow_refunds (
 id CHAR(36) NOT NULL PRIMARY KEY COMMENT '托管退款执行主键',
 case_id CHAR(36) NOT NULL COMMENT '关联售后争议案件ID',
 order_id CHAR(36) NOT NULL COMMENT '关联订单ID',
 buyer_id CHAR(36) NOT NULL COMMENT '接收退款的买家用户ID',
 operator_id CHAR(36) NOT NULL COMMENT '执行退款的运营管理员用户ID',
 request_id CHAR(36) NOT NULL COMMENT '执行请求幂等标识',
 request_hash CHAR(64) NOT NULL COMMENT '执行请求内容哈希，用于校验重试参数一致性',
 source_round INT NOT NULL COMMENT '退款依据的争议裁决轮次',
 currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '金额币种代码，当前为CNY',
 amount DECIMAL(12,2) NOT NULL COMMENT '实际退回买家钱包的货款金额，单位元',
 original_goods_amount DECIMAL(12,2) NOT NULL COMMENT '核验的订单原始货款金额，单位元',
 service_fee_held DECIMAL(12,2) NOT NULL COMMENT '仍保留托管的服务费金额，单位元',
 shipping_held DECIMAL(12,2) NOT NULL COMMENT '仍保留托管的运费金额，单位元',
 buyer_escrow_before DECIMAL(12,2) NOT NULL COMMENT '退款前买家托管余额，单位元',
 buyer_escrow_after DECIMAL(12,2) NOT NULL COMMENT '退款后买家托管余额，单位元',
 source_deposit_id CHAR(36) NOT NULL COMMENT '作为托管资金来源的保证金记录ID',
 wallet_ledger_id CHAR(36) NOT NULL COMMENT '实际退款入账对应的钱包流水ID',
 requires_accounting BOOLEAN NOT NULL COMMENT '是否需要进入核算阶段，1表示已退完剩余货款',
 source_snapshot JSON NOT NULL COMMENT '核验的托管资金来源JSON快照，用于资金审计，访问应受控',
 note VARCHAR(200) NOT NULL COMMENT '运营执行退款时填写的说明',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '退款执行记录创建时间',
 UNIQUE KEY uq_order_escrow_case(case_id),
 UNIQUE KEY uq_order_escrow_request(operator_id,request_id),
 UNIQUE KEY uq_order_escrow_ledger(wallet_ledger_id),
 INDEX idx_order_escrow_source(order_id,created_at,id),
 INDEX idx_order_escrow_buyer(buyer_id,created_at,id),
 CONSTRAINT chk_order_escrow_currency CHECK(currency='CNY'),
 CONSTRAINT chk_order_escrow_amount CHECK(amount>0 AND amount<=original_goods_amount AND buyer_escrow_after>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后裁决退回未花费货款托管资金至买家平台钱包的执行记录';
CREATE TABLE IF NOT EXISTS t_order_escrow_refund_events (
 id CHAR(36) NOT NULL PRIMARY KEY COMMENT '托管退款事件主键',
 execution_id CHAR(36) NOT NULL COMMENT '关联托管退款执行记录ID',
 actor_id CHAR(36) NOT NULL COMMENT '执行操作的用户ID',
 action VARCHAR(32) NOT NULL COMMENT '执行事件标识，当前写入wallet_refunded表示钱包已退款',
 detail VARCHAR(250) NOT NULL COMMENT '执行结果说明，用于区分平台钱包退款与外部银行退款',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件创建时间',
 INDEX idx_order_escrow_event(execution_id,created_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='托管货款退款执行的钱包入账审计事件';
