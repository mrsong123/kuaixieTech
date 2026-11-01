-- PROPOSAL ONLY: review against live information_schema and a verified backup before execution.
-- MySQL 8.0+. Select the existing business database in the client; no USE or database creation here.
-- No application startup runner executes this file. No existing table or row is changed.
-- CREATE IF NOT EXISTS does not validate an existing table: run the metadata preflight first.

CREATE TABLE IF NOT EXISTS t_withdrawals (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  channel VARCHAR(32) NOT NULL DEFAULT 'manual',
  status VARCHAR(16) NOT NULL DEFAULT 'pending',
  admin_note VARCHAR(2000) NULL,
  reviewed_by CHAR(36) NULL,
  reviewed_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  INDEX idx_withdrawals_user_status (user_id,status),
  INDEX idx_withdrawals_queue (status,created_at),
  CONSTRAINT chk_withdrawals_amount CHECK(amount > 0),
  CONSTRAINT chk_withdrawals_status CHECK(status IN ('pending','approved','rejected','paid')),
  CONSTRAINT fk_withdrawals_user FOREIGN KEY(user_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_announcements (
  id CHAR(36) NOT NULL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  body TEXT NOT NULL,
  kind VARCHAR(32) NOT NULL DEFAULT 'system',
  pinned TINYINT(1) NOT NULL DEFAULT 0,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_by CHAR(36) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  title_i18n JSON NULL,
  body_i18n JSON NULL,
  INDEX idx_announcements_public (active,pinned,created_at),
  CONSTRAINT fk_announcements_creator FOREIGN KEY(created_by) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_proof_recipes (
  id CHAR(36) NOT NULL PRIMARY KEY,
  trip_type VARCHAR(32) NOT NULL,
  transport_mode VARCHAR(32) NOT NULL,
  country_code VARCHAR(8) NULL,
  items JSON NOT NULL,
  note VARCHAR(2000) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 100,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX idx_proof_recipes_scope (trip_type,transport_mode,country_code,active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Compatibility settings for existing RPCs. These are not proof of provider connectivity.
-- Existing t_integration_* tables remain the vendor/product/field catalog.
-- Do not duplicate or move existing secret values into config. A unified runtime adapter is pending.
CREATE TABLE IF NOT EXISTS t_channel_configs (
  id CHAR(36) NOT NULL PRIMARY KEY,
  channel_type VARCHAR(32) NOT NULL,
  provider VARCHAR(64) NOT NULL,
  config JSON NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 0,
  remark VARCHAR(2000) NULL,
  updated_by CHAR(36) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_channel_provider (channel_type,provider),
  INDEX idx_channel_enabled (channel_type,enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_channel_secrets_probe (
  channel VARCHAR(32) NOT NULL PRIMARY KEY,
  ready TINYINT(1) NOT NULL DEFAULT 0,
  probed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_error_logs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  scope VARCHAR(128) NOT NULL,
  message TEXT NOT NULL,
  stack MEDIUMTEXT NULL,
  page VARCHAR(1024) NULL,
  user_id CHAR(36) NULL,
  extra JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'open',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  resolved_at DATETIME(3) NULL,
  INDEX idx_error_logs_queue (status,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_assist_requests (
  id CHAR(36) NOT NULL PRIMARY KEY,
  order_id CHAR(36) NOT NULL,
  requester_id CHAR(36) NOT NULL,
  assistant_id CHAR(36) NULL,
  share_rate DECIMAL(5,2) NOT NULL,
  note VARCHAR(2000) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'pending',
  handover_a_urls JSON NOT NULL,
  handover_a_city VARCHAR(64) NULL,
  handover_a_at DATETIME(3) NULL,
  handover_b_urls JSON NOT NULL,
  handover_b_city VARCHAR(64) NULL,
  finished_at DATETIME(3) NULL,
  settled_amount DECIMAL(12,2) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  INDEX idx_assist_order (order_id,status),
  INDEX idx_assist_assistant (assistant_id,status),
  CONSTRAINT chk_assist_share CHECK(share_rate >= 0 AND share_rate <= 100),
  CONSTRAINT chk_assist_status CHECK(status IN ('pending','accepted','carrying','done','cancelled')),
  CONSTRAINT fk_assist_order FOREIGN KEY(order_id) REFERENCES t_orders(id),
  CONSTRAINT fk_assist_requester FOREIGN KEY(requester_id) REFERENCES t_profiles(id),
  CONSTRAINT fk_assist_assistant FOREIGN KEY(assistant_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_enterprise_license_files (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  storage_url VARCHAR(1024) NOT NULL,
  position_no INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  INDEX idx_enterprise_license_user (user_id,position_no),
  CONSTRAINT fk_enterprise_license_user FOREIGN KEY(user_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_locales (
  code VARCHAR(16) NOT NULL PRIMARY KEY,
  native_name VARCHAR(64) NOT NULL,
  label_zh VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  active TINYINT(1) NOT NULL DEFAULT 1,
  fallback_code VARCHAR(16) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_logistics_tracking (
  order_id CHAR(36) NOT NULL PRIMARY KEY,
  provider VARCHAR(64) NULL,
  tracking_no VARCHAR(64) NULL,
  latest_state VARCHAR(64) NULL,
  events_json JSON NULL,
  refreshed_at DATETIME(3) NULL,
  CONSTRAINT fk_logistics_tracking_order FOREIGN KEY(order_id) REFERENCES t_orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
