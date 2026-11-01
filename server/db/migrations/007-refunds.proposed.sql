-- Deployment proposal only. Full original CNY recharge refunds, separate from goods/order refunds.
-- No existing payment status, wallet balance, business order or merchant credential is migrated.
CREATE TABLE IF NOT EXISTS t_payment_refunds (
 id CHAR(32) PRIMARY KEY,
 payment_id CHAR(32) NOT NULL,
 user_id CHAR(36) NOT NULL,
 request_id CHAR(36) NOT NULL,
 base_currency CHAR(3) NOT NULL,
 base_minor DECIMAL(24,0) NOT NULL,
 payment_currency CHAR(3) NOT NULL,
 payment_minor DECIMAL(24,0) NOT NULL,
 merchant_snapshot JSON NOT NULL,
 provider_transaction_id VARCHAR(128) NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'requested',
 reason VARCHAR(200) NOT NULL,
 review_note VARCHAR(200) NULL,
 reviewed_by CHAR(36) NULL,
 reviewed_at TIMESTAMP(3) NULL,
 dispatched_at TIMESTAMP(3) NULL,
 provider_refund_id VARCHAR(128) NULL,
 last_error_code VARCHAR(64) NULL,
 wallet_released BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 active_slot TINYINT GENERATED ALWAYS AS (CASE WHEN status IN('cancelled','rejected') AND dispatched_at IS NULL AND wallet_released=1 THEN NULL ELSE 1 END) STORED,
 UNIQUE KEY uq_payment_refund_source(payment_id,active_slot),
 UNIQUE KEY uq_payment_refund_request(user_id,request_id),
 INDEX idx_payment_refund_queue(status,created_at),
 CONSTRAINT chk_payment_refund_currency CHECK(base_currency='CNY' AND payment_currency='CNY'),
 CONSTRAINT chk_payment_refund_amount CHECK(base_minor>0 AND payment_minor>0),
 CONSTRAINT chk_payment_refund_status CHECK(status IN('requested','approved','dispatching','processing','succeeded','rejected','closed','cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_payment_refund_events (
 id CHAR(36) PRIMARY KEY,
 refund_id CHAR(32) NOT NULL,
 actor_id CHAR(36) NULL,
 action VARCHAR(32) NOT NULL,
 detail VARCHAR(250) NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 INDEX idx_payment_refund_event(refund_id,created_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
