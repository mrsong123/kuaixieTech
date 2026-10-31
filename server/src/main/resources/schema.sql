CREATE TABLE IF NOT EXISTS users (
 id CHAR(36) PRIMARY KEY, phone VARCHAR(32) UNIQUE NOT NULL, nickname VARCHAR(80), role VARCHAR(20) NOT NULL DEFAULT 'buyer',
 status VARCHAR(20) NOT NULL DEFAULT 'active', kyc_status VARCHAR(20) NOT NULL DEFAULT 'none', performance_score INT NOT NULL DEFAULT 60,
 balance DECIMAL(14,2) NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS user_preferences (
 user_id CHAR(36) PRIMARY KEY, locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN', country_code CHAR(2) NOT NULL DEFAULT 'CN', currency_code CHAR(3) NOT NULL DEFAULT 'CNY', time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai',
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS revoked_tokens (
 token_hash CHAR(64) PRIMARY KEY, expires_at DATETIME NOT NULL, revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_revoked_expiry(expires_at)
);
CREATE TABLE IF NOT EXISTS account_deletion_requests (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, reason VARCHAR(1000), status VARCHAR(24) NOT NULL DEFAULT 'submitted', operator_note VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(user_id) REFERENCES users(id), INDEX idx_account_deletion(user_id,status,created_at)
);
CREATE TABLE IF NOT EXISTS user_addresses (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, label VARCHAR(60) NOT NULL DEFAULT '收货地址', recipient_name VARCHAR(120) NOT NULL, phone VARCHAR(40) NOT NULL, country_code CHAR(2) NOT NULL, region VARCHAR(120), city VARCHAR(120), postal_code VARCHAR(32), address_line VARCHAR(500) NOT NULL, is_default BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, INDEX idx_user_addresses(user_id,is_default)
);
CREATE TABLE IF NOT EXISTS user_notifications (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, type VARCHAR(32) NOT NULL, title VARCHAR(200) NOT NULL, body VARCHAR(1000), target_path VARCHAR(500), is_read BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, read_at DATETIME,
 FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, INDEX idx_notifications_user(user_id,is_read,created_at)
);
CREATE TABLE IF NOT EXISTS categories (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, parent_id BIGINT NULL, level_no INT NOT NULL, name VARCHAR(100) NOT NULL,
 country VARCHAR(80) NOT NULL DEFAULT '通用', image_url VARCHAR(500), price_min DECIMAL(14,2), price_max DECIMAL(14,2), active BOOLEAN NOT NULL DEFAULT TRUE,
 INDEX idx_category_parent(parent_id), FOREIGN KEY(parent_id) REFERENCES categories(id)
);
CREATE TABLE IF NOT EXISTS products (
 id CHAR(36) PRIMARY KEY, owner_id CHAR(36), store_id CHAR(36), image_upload_id CHAR(36), category_id BIGINT, name VARCHAR(200) NOT NULL, description TEXT, spec VARCHAR(500), destination VARCHAR(80) NOT NULL,
 price DECIMAL(14,2) NOT NULL, stock INT NOT NULL DEFAULT 0, image_url VARCHAR(500), status VARCHAR(20) NOT NULL DEFAULT 'active', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(category_id) REFERENCES categories(id), INDEX idx_products_search(name,destination,status)
);
-- A product is not eligible for cross-border sale until a compliance reviewer
-- records its HS code, approved mode, port and official-list evidence.
CREATE TABLE IF NOT EXISTS cbec_product_reviews (
 product_id CHAR(36) PRIMARY KEY, hs_code VARCHAR(20) NOT NULL, import_mode VARCHAR(10) NOT NULL,
 port_code VARCHAR(40) NOT NULL, list_version VARCHAR(80) NOT NULL, official_source VARCHAR(800) NOT NULL,
 decision VARCHAR(20) NOT NULL DEFAULT 'pending', reviewed_by CHAR(36), reviewed_at DATETIME,
 review_note VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_cbec_mode CHECK(import_mode IN ('1210','9610')),
 CONSTRAINT chk_cbec_decision CHECK(decision IN ('pending','approved','rejected')),
 FOREIGN KEY(product_id) REFERENCES products(id), INDEX idx_cbec_decision(decision,import_mode)
);
CREATE TABLE IF NOT EXISTS trips (
 id CHAR(36) PRIMARY KEY, owner_id CHAR(36) NOT NULL, origin VARCHAR(100) NOT NULL, destination VARCHAR(100) NOT NULL, trip_type VARCHAR(20) NOT NULL,
 transport_mode VARCHAR(30) NOT NULL, transport_no VARCHAR(60), depart_at DATETIME NOT NULL, return_at DATETIME NULL, entry_port VARCHAR(100), capacity_qty INT NOT NULL DEFAULT 20,
 fee_rate DECIMAL(6,3) NOT NULL DEFAULT 5, carry_mode VARCHAR(20) NOT NULL DEFAULT 'solo', pool_slots INT, status VARCHAR(20) NOT NULL DEFAULT 'pending',
 visibility VARCHAR(20) NOT NULL DEFAULT 'public', ticket_proof_url VARCHAR(500), arrival_proof_url VARCHAR(500), arrival_at DATETIME, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_trip_browse(status,destination,depart_at)
);
CREATE TABLE IF NOT EXISTS demands (
 id CHAR(36) PRIMARY KEY, publisher_id CHAR(36) NOT NULL, title VARCHAR(200) NOT NULL, description TEXT, destination VARCHAR(100) NOT NULL, category_id BIGINT,
 expect_price DECIMAL(14,2), quantity INT NOT NULL DEFAULT 1, expect_days INT, trip_type VARCHAR(20) NOT NULL DEFAULT 'any', visibility VARCHAR(20) NOT NULL DEFAULT 'public',
 deadline DATETIME, status VARCHAR(20) NOT NULL DEFAULT 'open', tax_plan VARCHAR(30) DEFAULT 'any', insurance_pref VARCHAR(20) DEFAULT 'unselected', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_demand_browse(status,destination,deadline), FOREIGN KEY(category_id) REFERENCES categories(id)
);
CREATE TABLE IF NOT EXISTS quotes (
 id CHAR(36) PRIMARY KEY, demand_id CHAR(36) NOT NULL, vendor_id CHAR(36) NOT NULL, trip_id CHAR(36), price DECIMAL(14,2) NOT NULL,
 promise_days INT NOT NULL, purchase_place VARCHAR(200), note TEXT, status VARCHAR(20) NOT NULL DEFAULT 'submitted', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(demand_id) REFERENCES demands(id), INDEX idx_quotes_demand(demand_id)
);
CREATE TABLE IF NOT EXISTS orders (
 id CHAR(36) PRIMARY KEY, order_no VARCHAR(40) NOT NULL UNIQUE, buyer_id CHAR(36) NOT NULL, vendor_id CHAR(36), store_id CHAR(36), product_id CHAR(36), trip_id CHAR(36), demand_id CHAR(36), quote_id CHAR(36),
 item_name VARCHAR(200) NOT NULL, item_spec VARCHAR(500), quantity INT NOT NULL, unit_price DECIMAL(14,2) NOT NULL, goods_amount DECIMAL(14,2) NOT NULL,
 service_fee DECIMAL(14,2) NOT NULL, buyer_deposit DECIMAL(14,2) NOT NULL, vendor_deposit DECIMAL(14,2) NOT NULL DEFAULT 0, status VARCHAR(30) NOT NULL DEFAULT 'awaiting_vendor',
 delivery_mode VARCHAR(20) NOT NULL DEFAULT 'ship', meetup_code CHAR(4), meetup_expires_at DATETIME, tax_plan VARCHAR(30) NOT NULL DEFAULT 'vendor_keeps', tax_rate DECIMAL(7,5) DEFAULT 0,
 tax_refund_amount DECIMAL(14,2) DEFAULT 0, insured BOOLEAN NOT NULL DEFAULT FALSE, declared_value DECIMAL(14,2), insurance_premium DECIMAL(14,2) DEFAULT 0,
 payment_status VARCHAR(24) NOT NULL DEFAULT 'unpaid', insurance_policy_id CHAR(36),
 tracking_no VARCHAR(100), expected_flight_at DATETIME, accepted_at DATETIME, customs_released_at DATETIME,
 risk_notice_version VARCHAR(80), risk_notice_accepted_at DATETIME, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 INDEX idx_orders_buyer(buyer_id,status), INDEX idx_orders_vendor(vendor_id,status)
);
CREATE TABLE IF NOT EXISTS return_cases (
 id CHAR(36) PRIMARY KEY, order_id CHAR(36) NOT NULL, applicant_id CHAR(36) NOT NULL,
 reason VARCHAR(1000) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'requested',
 customs_application_at DATETIME, goods_arrived_at DATETIME,
 customs_application_deadline DATETIME NOT NULL, goods_arrival_deadline DATETIME NOT NULL,
 tax_benefit_eligible BOOLEAN NOT NULL DEFAULT TRUE,
 customs_reference VARCHAR(100), destination_facility VARCHAR(200), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(order_id) REFERENCES orders(id), INDEX idx_return_deadlines(status,customs_application_deadline,goods_arrival_deadline)
);
CREATE TABLE IF NOT EXISTS after_sales_cases (
 id CHAR(36) PRIMARY KEY, order_id CHAR(36) NOT NULL, applicant_id CHAR(36) NOT NULL,
 case_type VARCHAR(32) NOT NULL, reason VARCHAR(2000) NOT NULL, evidence_upload_ids JSON,
 status VARCHAR(24) NOT NULL DEFAULT 'submitted', merchant_response VARCHAR(2000), operator_note VARCHAR(1000),
 refund_status VARCHAR(32) NOT NULL DEFAULT 'provider_unavailable', refund_amount DECIMAL(14,2),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(order_id) REFERENCES orders(id), FOREIGN KEY(applicant_id) REFERENCES users(id),
 INDEX idx_after_sales_order(order_id,created_at), INDEX idx_after_sales_status(status,created_at)
);
CREATE TABLE IF NOT EXISTS consent_records (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, consent_type VARCHAR(40) NOT NULL,
 notice_version VARCHAR(80) NOT NULL, recipient_name VARCHAR(200), recipient_contact VARCHAR(300),
 purpose VARCHAR(500), information_types VARCHAR(1000), rights_method VARCHAR(1000),
 accepted BOOLEAN NOT NULL, accepted_at DATETIME NOT NULL, source VARCHAR(40) NOT NULL,
 INDEX idx_consent_user_type(user_id,consent_type,accepted_at)
);
CREATE TABLE IF NOT EXISTS t_auth_credentials (
 user_id CHAR(36) NOT NULL PRIMARY KEY, password_hash VARCHAR(120) NOT NULL,
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
);
CREATE TABLE IF NOT EXISTS login_codes (
 phone VARCHAR(32) PRIMARY KEY, code_hash CHAR(64) NOT NULL, expires_at DATETIME NOT NULL,
 attempts INT NOT NULL DEFAULT 0, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS auth_rate_limits (
 scope_name VARCHAR(32) NOT NULL, subject_hash CHAR(64) NOT NULL, used INT NOT NULL DEFAULT 0, window_end DATETIME NOT NULL,
 PRIMARY KEY(scope_name,subject_hash)
);
CREATE TABLE IF NOT EXISTS sms_send_events (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, phone_hash CHAR(64) NOT NULL, ip_hash CHAR(64) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_sms_event_created(created_at)
);
CREATE TABLE IF NOT EXISTS sms_daily_budget (budget_day DATE PRIMARY KEY, send_count INT NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS deposits (
 id CHAR(36) PRIMARY KEY, order_id CHAR(36) NOT NULL, user_id CHAR(36) NOT NULL, party_role VARCHAR(20) NOT NULL, amount DECIMAL(14,2) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'frozen', reason VARCHAR(100), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(order_id) REFERENCES orders(id), INDEX idx_deposit_user(user_id,status)
);
CREATE TABLE IF NOT EXISTS ledger (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id CHAR(36) NOT NULL, order_id CHAR(36), entry_type VARCHAR(30) NOT NULL, amount DECIMAL(14,2) NOT NULL,
 balance_after DECIMAL(14,2) NOT NULL, reference_id VARCHAR(80), description VARCHAR(300), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_ledger_user(user_id,created_at)
);
CREATE TABLE IF NOT EXISTS evidence (
 id CHAR(36) PRIMARY KEY, order_id CHAR(36), trip_id CHAR(36), user_id CHAR(36) NOT NULL, evidence_type VARCHAR(30) NOT NULL,
 file_url VARCHAR(800) NOT NULL, lat DECIMAL(10,7), lng DECIMAL(10,7), captured_at DATETIME, metadata JSON, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_evidence_order(order_id,evidence_type)
);
CREATE TABLE IF NOT EXISTS uploads (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, purpose VARCHAR(40) NOT NULL,
 order_id CHAR(36), trip_id CHAR(36), original_name VARCHAR(255) NOT NULL,
 content_type VARCHAR(120) NOT NULL, sha256 CHAR(64) NOT NULL, byte_size BIGINT NOT NULL,
 storage_path VARCHAR(700) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_upload_owner(user_id,created_at), INDEX idx_upload_order(order_id,purpose)
);
CREATE TABLE IF NOT EXISTS kyc_submissions (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL UNIQUE,
 id_front_upload_id CHAR(36) NOT NULL, id_back_upload_id CHAR(36) NOT NULL,
 residence_upload_id CHAR(36), travel_proof_upload_id CHAR(36), selfie_upload_id CHAR(36) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'pending', review_note VARCHAR(1000), reviewed_by CHAR(36), reviewed_at DATETIME,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 INDEX idx_kyc_status(status,created_at),
 CONSTRAINT chk_kyc_status CHECK(status IN ('pending','approved','rejected')),
 FOREIGN KEY(id_front_upload_id) REFERENCES uploads(id), FOREIGN KEY(id_back_upload_id) REFERENCES uploads(id),
 FOREIGN KEY(residence_upload_id) REFERENCES uploads(id), FOREIGN KEY(travel_proof_upload_id) REFERENCES uploads(id), FOREIGN KEY(selfie_upload_id) REFERENCES uploads(id)
);
CREATE TABLE IF NOT EXISTS store_applications (
 id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, business_name VARCHAR(200) NOT NULL,
 unified_social_credit_code VARCHAR(32) NOT NULL, legal_representative VARCHAR(100) NOT NULL,
 license_upload_id CHAR(36) NOT NULL, invitation_code VARCHAR(16), application_channel VARCHAR(20) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'pending', review_note VARCHAR(1000), reviewed_by CHAR(36), reviewed_at DATETIME,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 INDEX idx_store_app_status(status,created_at), INDEX idx_store_app_user(user_id,status),
 FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(license_upload_id) REFERENCES uploads(id)
);
CREATE TABLE IF NOT EXISTS stores (
 id CHAR(36) PRIMARY KEY, owner_id CHAR(36) NOT NULL UNIQUE, application_id CHAR(36) NOT NULL UNIQUE,
 name VARCHAR(200) NOT NULL, description VARCHAR(1000), matching_fee_rate DECIMAL(5,2) NOT NULL DEFAULT 5.00,
 status VARCHAR(20) NOT NULL DEFAULT 'active', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(owner_id) REFERENCES users(id), FOREIGN KEY(application_id) REFERENCES store_applications(id),
 CONSTRAINT chk_store_fee CHECK(matching_fee_rate >= 0 AND matching_fee_rate <= 10), INDEX idx_store_status(status)
);
CREATE TABLE IF NOT EXISTS store_insurance_policies (
 id CHAR(36) PRIMARY KEY, store_id CHAR(36) NOT NULL, insurer_name VARCHAR(200) NOT NULL, policy_no VARCHAR(120) NOT NULL,
 coverage_amount DECIMAL(14,2) NOT NULL, currency CHAR(3) NOT NULL DEFAULT 'CNY', expires_at DATE NOT NULL,
 certificate_upload_id CHAR(36) NOT NULL, status VARCHAR(40) NOT NULL DEFAULT 'pending_provider_verification',
 external_verification_ref VARCHAR(160), verification_channel VARCHAR(30), verified_by CHAR(36), verified_at DATETIME,
 review_note VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(store_id) REFERENCES stores(id), FOREIGN KEY(certificate_upload_id) REFERENCES uploads(id),
 INDEX idx_insurance_store_status(store_id,status,expires_at), UNIQUE KEY uq_insurer_policy(insurer_name,policy_no)
);
CREATE TABLE IF NOT EXISTS insurance_coverages (
 id CHAR(36) PRIMARY KEY, order_id CHAR(36) NOT NULL UNIQUE, policy_id CHAR(36) NOT NULL,
 external_coverage_ref VARCHAR(160) NOT NULL, declared_value DECIMAL(14,2) NOT NULL, premium_amount DECIMAL(14,2) NOT NULL,
 premium_paid_directly BOOLEAN NOT NULL, status VARCHAR(24) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(order_id) REFERENCES orders(id), FOREIGN KEY(policy_id) REFERENCES store_insurance_policies(id),
 INDEX idx_insurance_coverage_status(status,created_at)
);
CREATE TABLE IF NOT EXISTS disputes (
 id CHAR(36) PRIMARY KEY, order_id CHAR(36) NOT NULL, applicant_id CHAR(36) NOT NULL, reason VARCHAR(500) NOT NULL, evidence_json JSON,
 status VARCHAR(20) NOT NULL DEFAULT 'open', decision VARCHAR(30), decided_by CHAR(36), decision_note VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(order_id) REFERENCES orders(id)
);
CREATE TABLE IF NOT EXISTS messages (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id CHAR(36), sender_id CHAR(36) NOT NULL, body TEXT NOT NULL, translated_body TEXT, system_flag BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_messages_room(order_id,created_at)
);
CREATE TABLE IF NOT EXISTS invite_rewards (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, inviter_id CHAR(36) NOT NULL, invitee_id CHAR(36) NOT NULL, level_no INT NOT NULL,
 order_id CHAR(36), points INT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'pending', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_reward_level CHECK(level_no IN (1,2))
);
CREATE TABLE IF NOT EXISTS invite_codes (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, code VARCHAR(16) NOT NULL UNIQUE, phone VARCHAR(32) NOT NULL,
 created_by CHAR(36) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'issued', expires_at DATETIME NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_invite_phone(phone,status)
);
CREATE TABLE IF NOT EXISTS audit_logs (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, operator_id CHAR(36), action VARCHAR(100) NOT NULL, object_type VARCHAR(50) NOT NULL,
 object_id VARCHAR(80) NOT NULL, detail JSON, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS risk_pool (id TINYINT PRIMARY KEY, balance DECIMAL(14,2) NOT NULL DEFAULT 0);
INSERT IGNORE INTO risk_pool(id,balance) VALUES(1,0);

-- Community and direct messages. New posts are queued for operator moderation before public display.
CREATE TABLE IF NOT EXISTS community_posts (
 id CHAR(36) PRIMARY KEY, author_id CHAR(36) NOT NULL, title VARCHAR(200), body TEXT NOT NULL,
 post_type VARCHAR(16) NOT NULL DEFAULT 'text', status VARCHAR(24) NOT NULL DEFAULT 'pending_review', view_count BIGINT NOT NULL DEFAULT 0, deleted_at DATETIME,
 moderation_note VARCHAR(1000), moderated_by CHAR(36), moderated_at DATETIME,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(author_id) REFERENCES users(id), INDEX idx_community_feed(status,created_at)
);
CREATE TABLE IF NOT EXISTS community_media (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, post_id CHAR(36) NOT NULL, upload_id CHAR(36) NOT NULL, position_no INT NOT NULL DEFAULT 0,
 FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(upload_id) REFERENCES uploads(id),
 UNIQUE KEY uq_community_media(post_id,upload_id), INDEX idx_community_media_upload(upload_id)
);
CREATE TABLE IF NOT EXISTS community_likes (
 post_id CHAR(36) NOT NULL, user_id CHAR(36) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(post_id,user_id), FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS community_post_views (
 post_id CHAR(36) NOT NULL, viewer_id CHAR(36) NOT NULL, view_day DATE NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(post_id,viewer_id,view_day), FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(viewer_id) REFERENCES users(id), INDEX idx_post_views_day(view_day)
);
CREATE TABLE IF NOT EXISTS community_saves (
 post_id CHAR(36) NOT NULL, user_id CHAR(36) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(post_id,user_id), FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS community_shares (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, post_id CHAR(36) NOT NULL, user_id CHAR(36) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(user_id) REFERENCES users(id), INDEX idx_community_share(post_id,created_at)
);
CREATE TABLE IF NOT EXISTS community_comments (
 id CHAR(36) PRIMARY KEY, post_id CHAR(36) NOT NULL, user_id CHAR(36) NOT NULL, parent_id CHAR(36), body VARCHAR(2000) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'published', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(user_id) REFERENCES users(id),
 FOREIGN KEY(parent_id) REFERENCES community_comments(id), INDEX idx_community_comments(post_id,status,created_at)
);
CREATE TABLE IF NOT EXISTS community_reports (
 id CHAR(36) PRIMARY KEY, post_id CHAR(36) NOT NULL, reporter_id CHAR(36) NOT NULL, reason VARCHAR(1000) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'open', reviewed_by CHAR(36), reviewed_at DATETIME, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(post_id) REFERENCES community_posts(id) ON DELETE CASCADE, FOREIGN KEY(reporter_id) REFERENCES users(id), INDEX idx_community_reports(status,created_at)
);
CREATE TABLE IF NOT EXISTS community_conversations (
 id CHAR(36) PRIMARY KEY, participant_a CHAR(36) NOT NULL, participant_b CHAR(36) NOT NULL, post_id CHAR(36),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(participant_a) REFERENCES users(id), FOREIGN KEY(participant_b) REFERENCES users(id), FOREIGN KEY(post_id) REFERENCES community_posts(id),
 UNIQUE KEY uq_community_conversation(participant_a,participant_b,post_id), INDEX idx_conversation_a(participant_a,updated_at), INDEX idx_conversation_b(participant_b,updated_at)
);
CREATE TABLE IF NOT EXISTS community_messages (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, conversation_id CHAR(36) NOT NULL, sender_id CHAR(36) NOT NULL,
 body VARCHAR(4000) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(conversation_id) REFERENCES community_conversations(id) ON DELETE CASCADE, FOREIGN KEY(sender_id) REFERENCES users(id),
 INDEX idx_community_messages(conversation_id,created_at)
);
INSERT IGNORE INTO categories(id,parent_id,level_no,name,country,price_min,price_max) VALUES
(1,NULL,1,'日本','日本',NULL,NULL),(2,NULL,1,'韩国','韩国',NULL,NULL),(3,NULL,1,'法国','法国',NULL,NULL),(4,NULL,1,'中国','中国',NULL,NULL),
(10,1,2,'美妆护肤','日本',120,2800),(11,1,2,'箱包配饰','日本',680,12000),(12,1,2,'数码电子','日本',480,8900),
(20,2,2,'美妆护肤','韩国',80,1800),(21,2,2,'食品保健','韩国',60,900),(30,3,2,'特色好物','法国',200,20000);

-- Resolve forward references after stores and insurance tables have been created.
SET @ddl = (SELECT IF(COUNT(*)=0, 'ALTER TABLE products ADD CONSTRAINT fk_products_store FOREIGN KEY(store_id) REFERENCES stores(id)', 'SELECT 1') FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='products' AND constraint_name='fk_products_store');
PREPARE codex_stmt FROM @ddl; EXECUTE codex_stmt; DEALLOCATE PREPARE codex_stmt;
SET @ddl = (SELECT IF(COUNT(*)=0, 'ALTER TABLE products ADD CONSTRAINT fk_products_image_upload FOREIGN KEY(image_upload_id) REFERENCES uploads(id)', 'SELECT 1') FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='products' AND constraint_name='fk_products_image_upload');
PREPARE codex_stmt FROM @ddl; EXECUTE codex_stmt; DEALLOCATE PREPARE codex_stmt;
SET @ddl = (SELECT IF(COUNT(*)=0, 'ALTER TABLE orders ADD CONSTRAINT fk_orders_store FOREIGN KEY(store_id) REFERENCES stores(id)', 'SELECT 1') FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='orders' AND constraint_name='fk_orders_store');
PREPARE codex_stmt FROM @ddl; EXECUTE codex_stmt; DEALLOCATE PREPARE codex_stmt;
SET @ddl = (SELECT IF(COUNT(*)=0, 'ALTER TABLE orders ADD CONSTRAINT fk_orders_product FOREIGN KEY(product_id) REFERENCES products(id)', 'SELECT 1') FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='orders' AND constraint_name='fk_orders_product');
PREPARE codex_stmt FROM @ddl; EXECUTE codex_stmt; DEALLOCATE PREPARE codex_stmt;
SET @ddl = (SELECT IF(COUNT(*)=0, 'ALTER TABLE orders ADD CONSTRAINT fk_orders_insurance_policy FOREIGN KEY(insurance_policy_id) REFERENCES store_insurance_policies(id)', 'SELECT 1') FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='orders' AND constraint_name='fk_orders_insurance_policy');
PREPARE codex_stmt FROM @ddl; EXECUTE codex_stmt; DEALLOCATE PREPARE codex_stmt;
