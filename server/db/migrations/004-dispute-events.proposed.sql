-- PROPOSAL ONLY: append-only dispute history and request deduplication.
-- No business rows, wallet balances or insurer funds are modified by this script.
CREATE TABLE IF NOT EXISTS t_dispute_events (
 id CHAR(36) NOT NULL PRIMARY KEY,
 dispute_id CHAR(36) NOT NULL,
 order_id CHAR(36) NOT NULL,
 actor_id CHAR(36) NOT NULL,
 actor_role VARCHAR(16) NOT NULL,
 action VARCHAR(32) NOT NULL,
 request_id CHAR(36) NOT NULL,
 before_stage VARCHAR(24) NULL,
 after_stage VARCHAR(24) NOT NULL,
 payload JSON NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uq_dispute_action_request(dispute_id,actor_id,request_id),
 INDEX idx_dispute_event_history(dispute_id,created_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
