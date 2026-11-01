-- Deployment proposal only: proven goods-escrow wallet refunds, never original payment-channel refunds.
-- Does not backfill legacy orders, move balances, invent insurance protection or change fee policy.
CREATE TABLE IF NOT EXISTS t_order_escrow_refunds (
 id CHAR(36) NOT NULL PRIMARY KEY,
 case_id CHAR(36) NOT NULL,
 order_id CHAR(36) NOT NULL,
 buyer_id CHAR(36) NOT NULL,
 operator_id CHAR(36) NOT NULL,
 request_id CHAR(36) NOT NULL,
 request_hash CHAR(64) NOT NULL,
 source_round INT NOT NULL,
 currency CHAR(3) NOT NULL DEFAULT 'CNY',
 amount DECIMAL(12,2) NOT NULL,
 original_goods_amount DECIMAL(12,2) NOT NULL,
 service_fee_held DECIMAL(12,2) NOT NULL,
 shipping_held DECIMAL(12,2) NOT NULL,
 buyer_escrow_before DECIMAL(12,2) NOT NULL,
 buyer_escrow_after DECIMAL(12,2) NOT NULL,
 source_deposit_id CHAR(36) NOT NULL,
 wallet_ledger_id CHAR(36) NOT NULL,
 requires_accounting BOOLEAN NOT NULL,
 source_snapshot JSON NOT NULL,
 note VARCHAR(200) NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uq_order_escrow_case(case_id),
 UNIQUE KEY uq_order_escrow_request(operator_id,request_id),
 UNIQUE KEY uq_order_escrow_ledger(wallet_ledger_id),
 INDEX idx_order_escrow_source(order_id,created_at,id),
 INDEX idx_order_escrow_buyer(buyer_id,created_at,id),
 CONSTRAINT chk_order_escrow_currency CHECK(currency='CNY'),
 CONSTRAINT chk_order_escrow_amount CHECK(amount>0 AND amount<=original_goods_amount AND buyer_escrow_after>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_order_escrow_refund_events (
 id CHAR(36) NOT NULL PRIMARY KEY,
 execution_id CHAR(36) NOT NULL,
 actor_id CHAR(36) NOT NULL,
 action VARCHAR(32) NOT NULL,
 detail VARCHAR(250) NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 INDEX idx_order_escrow_event(execution_id,created_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
