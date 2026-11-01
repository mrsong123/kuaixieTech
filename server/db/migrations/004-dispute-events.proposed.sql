-- PROPOSAL ONLY: append-only dispute history and request deduplication.
-- No business rows, wallet balances or insurer funds are modified by this script.
CREATE TABLE IF NOT EXISTS t_dispute_events (
 id CHAR(36) NOT NULL PRIMARY KEY COMMENT '争议事件主键',
 dispute_id CHAR(36) NOT NULL COMMENT '关联争议案件ID',
 order_id CHAR(36) NOT NULL COMMENT '关联订单ID',
 actor_id CHAR(36) NOT NULL COMMENT '执行操作的用户ID',
 actor_role VARCHAR(16) NOT NULL COMMENT '操作者在争议中的角色标识',
 action VARCHAR(32) NOT NULL COMMENT '争议操作标识',
 request_id CHAR(36) NOT NULL COMMENT '操作请求幂等标识',
 before_stage VARCHAR(24) NULL COMMENT '操作前的争议阶段，首次事件可为空',
 after_stage VARCHAR(24) NOT NULL COMMENT '操作后的争议阶段',
 payload JSON NOT NULL COMMENT '事件输入与结果等JSON载荷，可能含争议证据及请求摘要，访问应受控',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件创建时间',
 UNIQUE KEY uq_dispute_action_request(dispute_id,actor_id,request_id),
 INDEX idx_dispute_event_history(dispute_id,created_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单争议处理操作及阶段变更审计事件';
