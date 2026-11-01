-- PROPOSAL ONLY. Existing business database; MySQL 8+. No balances are migrated or modified.
-- Review live metadata before application. No application startup executes this file.
CREATE TABLE IF NOT EXISTS t_payment_merchants (
 id CHAR(36) PRIMARY KEY, name VARCHAR(80) NOT NULL, adapter VARCHAR(40) NOT NULL,
 method VARCHAR(24) NOT NULL, platform VARCHAR(24) NOT NULL, currencies JSON NOT NULL,
 credential_prefix VARCHAR(40) NULL, spread_bps INT NOT NULL DEFAULT 0,
 quote_ttl_seconds INT NOT NULL DEFAULT 300, enabled BOOLEAN NOT NULL DEFAULT FALSE,
 revision BIGINT NOT NULL DEFAULT 1, settings JSON NOT NULL, updated_by CHAR(36) NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 CONSTRAINT chk_payment_spread CHECK(spread_bps BETWEEN 0 AND 2000),
 CONSTRAINT chk_payment_ttl CHECK(quote_ttl_seconds BETWEEN 30 AND 900)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_fx_sources (
 id CHAR(36) PRIMARY KEY, name VARCHAR(80) NOT NULL, kind VARCHAR(32) NOT NULL,
 endpoint VARCHAR(1000) NULL, credential_prefix VARCHAR(40) NULL,
 max_age_seconds INT NOT NULL DEFAULT 86400, enabled BOOLEAN NOT NULL DEFAULT FALSE,
 transaction_enabled BOOLEAN NOT NULL DEFAULT FALSE, updated_by CHAR(36) NOT NULL,
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_payment_quotes (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, merchant_id CHAR(36) NOT NULL,
 merchant_revision BIGINT NOT NULL, merchant_snapshot JSON NOT NULL,
 base_currency CHAR(3) NOT NULL DEFAULT 'CNY', base_minor DECIMAL(24,0) NOT NULL,
 payment_currency CHAR(3) NOT NULL, payment_minor DECIMAL(24,0) NOT NULL, payment_minor_digits INT NOT NULL,
 fx_rate DECIMAL(36,18) NOT NULL, spread_bps INT NOT NULL, rate_source VARCHAR(80) NOT NULL,
 rate_as_of TIMESTAMP(3) NOT NULL, expires_at TIMESTAMP(3) NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 INDEX idx_payment_quotes_user(user_id,created_at),
 CONSTRAINT chk_quote_cny CHECK(base_currency='CNY'),
 CONSTRAINT chk_quote_amount CHECK(base_minor>0 AND payment_minor>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_currency_payments (
 id CHAR(32) PRIMARY KEY, user_id CHAR(36) NOT NULL, request_id CHAR(36) NOT NULL,
 quote_id CHAR(36) NOT NULL, merchant_id CHAR(36) NOT NULL, merchant_snapshot JSON NOT NULL,
 base_currency CHAR(3) NOT NULL DEFAULT 'CNY', base_minor DECIMAL(24,0) NOT NULL,
 payment_currency CHAR(3) NOT NULL, payment_minor DECIMAL(24,0) NOT NULL, payment_minor_digits INT NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'creating', checkout_kind VARCHAR(24) NULL,
 checkout_value TEXT NULL, provider_transaction_id VARCHAR(128) NULL,
 last_checked_at TIMESTAMP(3) NULL, last_error_code VARCHAR(64) NULL,
 paid_at TIMESTAMP(3) NULL, created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_currency_payment_request(user_id,request_id), UNIQUE KEY uk_currency_payment_quote(quote_id),
 UNIQUE KEY uk_currency_payment_transaction(merchant_id,provider_transaction_id),
 INDEX idx_currency_payment_pending(status,created_at),
 CONSTRAINT chk_currency_payment_status CHECK(status IN('creating','pending','paid')),
 CONSTRAINT chk_currency_payment_cny CHECK(base_currency='CNY'),
 CONSTRAINT chk_currency_payment_amount CHECK(base_minor>0 AND payment_minor>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_currency_payment_receipts (
 id CHAR(36) PRIMARY KEY, payment_id CHAR(32) NOT NULL,
 merchant_id CHAR(36) NOT NULL, provider_transaction_id VARCHAR(128) NOT NULL,
 payment_currency CHAR(3) NOT NULL, payment_minor DECIMAL(24,0) NOT NULL, payment_minor_digits INT NOT NULL,
 base_minor DECIMAL(24,0) NOT NULL, verified_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_currency_receipt_payment(payment_id),
 UNIQUE KEY uk_currency_receipt_provider(merchant_id,provider_transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- Intentionally no default enabled merchant, no sample exchange rates, no fake credentials.
