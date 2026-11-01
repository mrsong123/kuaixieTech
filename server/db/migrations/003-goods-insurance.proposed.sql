-- PROPOSAL ONLY. Existing insurance/store policy tables and historical balances are not rewritten.
-- Consumer insurance is issued by a contracted insurer, paid directly to that insurer.
CREATE TABLE IF NOT EXISTS t_goods_insurance_products (
 id VARCHAR(64) PRIMARY KEY,
 insurer_code VARCHAR(64) NOT NULL,
 config_json JSON NOT NULL,
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 revision BIGINT NOT NULL DEFAULT 1,
 updated_by CHAR(36) NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_goods_insurance_quotes (
 id CHAR(36) PRIMARY KEY,
 user_id CHAR(36) NOT NULL,
 subject_type VARCHAR(16) NOT NULL,
 platform VARCHAR(16) NOT NULL,
 subject_id CHAR(36) NOT NULL,
 item_name VARCHAR(255) NOT NULL,
 product_id VARCHAR(64) NOT NULL,
 product_revision BIGINT NOT NULL,
 declared_value DECIMAL(14,2) NOT NULL,
 premium DECIMAL(24,6) NOT NULL,
 currency CHAR(3) NOT NULL,
 provider_ref VARCHAR(160) NOT NULL,
 terms_version VARCHAR(160) NOT NULL,
 snapshot_json JSON NOT NULL,
 expires_at DATETIME(3) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 INDEX idx_goods_insurance_quote_user(user_id,created_at),
 UNIQUE KEY uq_goods_insurance_quote_ref(product_id,provider_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_goods_insurance_applications (
 id CHAR(36) PRIMARY KEY,
 user_id CHAR(36) NOT NULL,
 order_id CHAR(36) NOT NULL,
 quote_id CHAR(36) NOT NULL,
 insurer_code VARCHAR(64) NOT NULL,
 request_id CHAR(36) NOT NULL,
 status VARCHAR(32) NOT NULL DEFAULT 'reserved',
 provider_application_ref VARCHAR(160) NULL,
 checkout_url VARCHAR(1500) NULL,
 policy_number VARCHAR(160) NULL,
 coverage_ref VARCHAR(160) NULL,
 certificate_url VARCHAR(1500) NULL,
 coverage_starts_at DATETIME(3) NULL,
 coverage_ends_at DATETIME(3) NULL,
 terms_accepted_at DATETIME(3) NOT NULL,
 disclosures_accepted_at DATETIME(3) NOT NULL,
 last_checked_at DATETIME(3) NULL,
 last_error_code VARCHAR(64) NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uq_goods_insurance_order(order_id),
 UNIQUE KEY uq_goods_insurance_quote(quote_id),
 UNIQUE KEY uq_goods_insurance_request(user_id,request_id),
 UNIQUE KEY uq_goods_insurance_coverage(insurer_code,coverage_ref),
 UNIQUE KEY uq_goods_insurance_provider_application(insurer_code,provider_application_ref),
 INDEX idx_goods_insurance_pending(status,last_checked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
