-- Deployment proposal only. Full original CNY recharge refunds, separate from goods/order refunds.
-- No existing payment status, wallet balance, business order or merchant credential is migrated.
CREATE TABLE IF NOT EXISTS t_payment_refunds (
 id CHAR(32) PRIMARY KEY COMMENT '退款单主键，32位标识',
 payment_id CHAR(32) NOT NULL COMMENT '原充值支付单ID',
 user_id CHAR(36) NOT NULL COMMENT '申请退款的用户ID',
 request_id CHAR(36) NOT NULL COMMENT '用户退款请求幂等标识',
 base_currency CHAR(3) NOT NULL COMMENT '退款基准币种代码，当前退款仅支持CNY',
 base_minor DECIMAL(24,0) NOT NULL COMMENT '基准退款金额的最小货币单位整数，CNY时单位分',
 payment_currency CHAR(3) NOT NULL COMMENT '原渠道支付及退款币种代码',
 payment_minor DECIMAL(24,0) NOT NULL COMMENT '渠道退款金额的最小货币单位整数，当前支持CNY分',
 merchant_snapshot JSON NOT NULL COMMENT '原支付商户配置JSON快照，用于原路退款，敏感配置受控访问',
 provider_transaction_id VARCHAR(128) NOT NULL COMMENT '原渠道支付交易流水号',
 status VARCHAR(16) NOT NULL DEFAULT 'requested' COMMENT '退款状态：requested待审核、approved已批准、rejected已驳回、cancelled已撤回、dispatching派发中、processing渠道处理中、succeeded成功、closed关闭',
 reason VARCHAR(200) NOT NULL COMMENT '用户申请退款原因',
 review_note VARCHAR(200) NULL COMMENT '管理员审核说明',
 reviewed_by CHAR(36) NULL COMMENT '审核管理员用户ID',
 reviewed_at TIMESTAMP(3) NULL COMMENT '审核时间',
 dispatched_at TIMESTAMP(3) NULL COMMENT '占用渠道派发请求的时间，记录后不得当作未派发申请重试',
 provider_refund_id VARCHAR(128) NULL COMMENT '渠道退款流水号',
 last_error_code VARCHAR(64) NULL COMMENT '最近渠道操作的安全错误码',
 wallet_released BOOLEAN NOT NULL DEFAULT FALSE COMMENT '退款预留的钱包余额是否已释放，1已释放、0未释放',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 active_slot TINYINT GENERATED ALWAYS AS (CASE WHEN status IN('cancelled','rejected') AND dispatched_at IS NULL AND wallet_released=1 THEN NULL ELSE 1 END) STORED COMMENT '自动生成的退款占用槽；撤回或驳回且未派发并已释放钱包预留时为空，其余为1，用于限制同笔支付重复退款',
 UNIQUE KEY uq_payment_refund_source(payment_id,active_slot),
 UNIQUE KEY uq_payment_refund_request(user_id,request_id),
 INDEX idx_payment_refund_queue(status,created_at),
 CONSTRAINT chk_payment_refund_currency CHECK(base_currency='CNY' AND payment_currency='CNY'),
 CONSTRAINT chk_payment_refund_amount CHECK(base_minor>0 AND payment_minor>0),
 CONSTRAINT chk_payment_refund_status CHECK(status IN('requested','approved','dispatching','processing','succeeded','rejected','closed','cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值原路退款申请、审核、渠道派发与钱包冻结释放记录';
CREATE TABLE IF NOT EXISTS t_payment_refund_events (
 id CHAR(36) PRIMARY KEY COMMENT '退款事件主键',
 refund_id CHAR(32) NOT NULL COMMENT '关联退款单ID',
 actor_id CHAR(36) NULL COMMENT '执行操作的用户ID，渠道自动事件可为空',
 action VARCHAR(32) NOT NULL COMMENT '退款操作或渠道结果标识',
 detail VARCHAR(250) NOT NULL COMMENT '退款事件说明，记录核验结果而非渠道敏感报文',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件创建时间',
 INDEX idx_payment_refund_event(refund_id,created_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值退款审核、派发及渠道结果审计事件';
