-- 数据库：suixinggou；随行带货交易撮合平台业务库。
-- 完整结构版：原有82表 + 提议新增31表 = 113表，含表及字段中文COMMENT。
-- 仅供新建空库审阅初始化；已有业务库请使用增量版，不要重复导入本文件。
-- 保留原始索引、外键及触发器定义；不含原始种子数据和业务数据。
-- MySQL没有库级COMMENT语法，数据库用途在此及DATA_DICTIONARY.md中说明。
-- 本工具不连接数据库；未验证原始触发器或实际MySQL执行。
-- 原始文件SHA256：d9d449ab5bade1850c714ac9fb7ab56a85feca866cc988569058283fab478f2a；排除种子INSERT：9条。


CREATE DATABASE IF NOT EXISTS `suixinggou` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `suixinggou`;

-- 完整结构版仅允许空库；请让SQL客户端遇错停止，不要使用 --force。
SET @kuaixie_full_schema_guard = IF(
 (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE())=0,
 'SELECT ''Empty database confirmed'' AS initialization_status',
 'SELECT * FROM information_schema.KUAIXIE_FULL_SCHEMA_REQUIRES_EMPTY_DATABASE');
PREPARE kuaixie_full_schema_guard_stmt FROM @kuaixie_full_schema_guard;
EXECUTE kuaixie_full_schema_guard_stmt;
DEALLOCATE PREPARE kuaixie_full_schema_guard_stmt;
SET @kuaixie_full_schema_guard = NULL;

SET NAMES utf8mb4;
SET @merged_previous_fk_checks = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ===== [1/6] suixinggou.sql：基础域、交易域、治理域 (含条件外键) =====

CREATE TABLE IF NOT EXISTS t_profiles (
  id CHAR(36) NOT NULL COMMENT '主键，Spring Security 用户主键',
  username VARCHAR(64) NULL COMMENT '用户名',
  openid VARCHAR(128) NULL COMMENT '微信 openid',
  unionid VARCHAR(128) NULL COMMENT '微信 unionid',
  nickname VARCHAR(64) NULL COMMENT '昵称',
  avatar_url VARCHAR(512) NULL COMMENT '头像 URL',
  phone VARCHAR(32) NULL COMMENT '手机号',
  city VARCHAR(64) NULL COMMENT '常驻城市',
  is_admin TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否运营账号',
  invite_code_used VARCHAR(32) NULL COMMENT '注册时使用的邀请码',
  carrier_verify_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT '带货人实名认证审核状态，默认 none',
  carrier_verify_reason VARCHAR(255) NULL COMMENT '带货人认证驳回原因',
  verified_at DATETIME(3) NULL COMMENT '带货人认证通过时间',
  performance_score INT NOT NULL DEFAULT 60 COMMENT '履约分',
  star_avg DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '平均星级',
  star_count INT NOT NULL DEFAULT 0 COMMENT '评价总数',
  deal_count INT NOT NULL DEFAULT 0 COMMENT '累计成交单数',
  ontime_count INT NOT NULL DEFAULT 0 COMMENT '按时履约次数',
  breach_count_90d INT NOT NULL DEFAULT 0 COMMENT '近90天违约次数',
  abandon_count_90d INT NOT NULL DEFAULT 0 COMMENT '近90天爽约次数',
  lost_dispute_count_90d INT NOT NULL DEFAULT 0 COMMENT '近90天败诉纠纷次数',
  clean_streak INT NOT NULL DEFAULT 0 COMMENT '连续无违规单数',
  dispute_reject_streak INT NOT NULL DEFAULT 0 COMMENT '纠纷连续驳回次数',
  banned_until DATETIME(3) NULL COMMENT '封禁截止时间',
  boost_factor DECIMAL(4,2) NOT NULL DEFAULT 1.00 COMMENT '流量加权系数',
  balance DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额（元）',
  payout_account VARCHAR(128) NULL COMMENT '用户提现收款账户，属于敏感账户信息',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  my_invite_code VARCHAR(32) NULL COMMENT '我的专属邀请码（唯一）',
  invited_by CHAR(36) NULL COMMENT '邀请人 → t_profiles.id',
  points INT NOT NULL DEFAULT 0 COMMENT '积分余额',
  real_name VARCHAR(64) NULL COMMENT '用户实名姓名，属于个人敏感信息',
  id_card_no_masked VARCHAR(32) NULL COMMENT '脱敏身份证号码，不用于保存完整证件号',
  passport_no_masked VARCHAR(32) NULL COMMENT '脱敏护照号码，不用于保存完整证件号',
  email VARCHAR(128) NULL COMMENT '邮箱',
  phone_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '手机号已验证',
  email_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮箱已验证',
  user_role VARCHAR(16) NOT NULL DEFAULT 'buyer' COMMENT '注册身份 buyer/carrier/enterprise',
  buyer_verify_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT '买家轻量实名认证审核状态，默认 none',
  buyer_verified_at DATETIME(3) NULL COMMENT '买家实名通过时间',
  carrier_tier VARCHAR(16) NOT NULL DEFAULT 'casual' COMMENT '带货人档位 casual/pro',
  buyer_verify_reason VARCHAR(255) NULL COMMENT '买家实名驳回原因',
  invite_tier VARCHAR(8) NOT NULL DEFAULT 'l0' COMMENT '分销等级 l0~l4',
  valid_invitee_count INT NOT NULL DEFAULT 0 COMMENT '有效邀请人数',
  commission_mtd DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '本月已计团队提成金额（元）',
  commission_mtd_month VARCHAR(7) NULL COMMENT '团队提成统计月份（YYYY-MM）',
  gender VARCHAR(8) NULL COMMENT '性别',
  notify_order TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知偏好：订单动态',
  notify_logistics TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知偏好：物流动态',
  notify_commission TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知偏好：提成到账',
  notify_system TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知偏好：系统消息',
  consent_location TINYINT(1) NOT NULL DEFAULT 1 COMMENT '隐私授权：定位',
  consent_camera TINYINT(1) NOT NULL DEFAULT 1 COMMENT '隐私授权：摄像头',
  consent_album TINYINT(1) NOT NULL DEFAULT 1 COMMENT '隐私授权：相册',
  personalized_reco TINYINT(1) NOT NULL DEFAULT 1 COMMENT '个性化推荐开关',
  deleted_at DATETIME(3) NULL COMMENT '注销软删时间',
  consent_audio TINYINT(1) NOT NULL DEFAULT 1 COMMENT '隐私授权：录音',
  enterprise_name VARCHAR(128) NULL COMMENT '企业名称',
  business_license_no VARCHAR(64) NULL COMMENT '统一社会信用代码',
  enterprise_verify_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT '企业账号认证审核状态，默认 none',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_profiles_username (username),
  UNIQUE KEY uk_t_profiles_openid (openid),
  UNIQUE KEY uk_t_profiles_unionid (unionid),
  UNIQUE KEY uk_t_profiles_my_invite_code (my_invite_code),
  KEY idx_t_profiles_invited_by (invited_by),
  CONSTRAINT fk_t_profiles_invited_by FOREIGN KEY (invited_by) REFERENCES t_profiles (id) ON DELETE SET NULL,
  CONSTRAINT chk_t_profiles_buyer_verify CHECK (buyer_verify_status IN ('none','pending','approved','rejected')),
  CONSTRAINT chk_t_profiles_invite_tier CHECK (invite_tier IN ('l0','l1','l2','l3','l4')),
  CONSTRAINT chk_t_profiles_user_role CHECK (user_role IN ('buyer','carrier','enterprise')),
  CONSTRAINT chk_t_profiles_ent_verify CHECK (enterprise_verify_status IN ('none','pending','approved','rejected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主档';

CREATE TABLE IF NOT EXISTS t_verification_codes (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  target VARCHAR(128) NOT NULL COMMENT '目标（手机号/邮箱/uid）',
  channel VARCHAR(32) NOT NULL COMMENT '通道 sms/email/face_<scene>',
  code VARCHAR(16) NOT NULL COMMENT '验证码或刷脸口令内容，属于验证凭据，不应向无关用户披露',
  purpose VARCHAR(32) NOT NULL DEFAULT 'contact_verify' COMMENT '验证码或刷脸口令的业务用途，默认 contact_verify',
  expires_at DATETIME(3) NOT NULL COMMENT '过期时间',
  used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已消费',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '签发时间',
  PRIMARY KEY (id),
  KEY idx_t_verification_codes_target (target, channel, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码与刷脸口令';

CREATE TABLE IF NOT EXISTS t_kyc_documents (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '上传人',
  doc_type VARCHAR(32) NOT NULL COMMENT '实名认证上传材料的类型标识',
  storage_url VARCHAR(512) NOT NULL COMMENT '存储桶 URL',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '实名认证材料审核状态，默认 pending',
  reject_reason VARCHAR(255) NULL COMMENT '驳回原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  challenge_code VARCHAR(32) NULL COMMENT '上传认证材料时使用的挑战口令',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_kyc_documents_user_doc (user_id, doc_type),
  KEY idx_t_kyc_documents_user (user_id, status),
  CONSTRAINT fk_t_kyc_documents_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件类 KYC 材料';

CREATE TABLE IF NOT EXISTS t_face_verifications (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '刷脸人',
  scene VARCHAR(32) NOT NULL COMMENT '刷脸留证的业务场景标识',
  snapshot_url VARCHAR(512) NOT NULL COMMENT '刷脸自拍留证图片地址，属于敏感认证材料',
  device_info VARCHAR(255) NULL COMMENT '设备信息',
  result VARCHAR(16) NOT NULL DEFAULT 'passed' COMMENT '刷脸留证结果标识，默认 passed',
  order_id CHAR(36) NULL COMMENT '关联订单',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  challenge_code VARCHAR(32) NULL COMMENT '手写纸条口令',
  PRIMARY KEY (id),
  KEY idx_t_face_verifications_user (user_id, created_at),
  KEY idx_t_face_verifications_order (order_id),
  CONSTRAINT fk_t_face_verifications_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷脸留证记录';

CREATE TABLE IF NOT EXISTS t_addresses (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '所属用户',
  contact_name VARCHAR(64) NOT NULL COMMENT '联系人',
  contact_phone VARCHAR(32) NOT NULL COMMENT '联系电话',
  province VARCHAR(64) NULL COMMENT '省',
  city VARCHAR(64) NULL COMMENT '市',
  district VARCHAR(64) NULL COMMENT '区',
  detail VARCHAR(512) NOT NULL COMMENT '详细地址',
  is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认地址',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_t_addresses_user (user_id),
  CONSTRAINT fk_t_addresses_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家收货地址簿';

CREATE TABLE IF NOT EXISTS t_pay_orders (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '充值用户',
  out_trade_no VARCHAR(64) NOT NULL COMMENT '商户订单号（唯一）',
  prepay_id VARCHAR(128) NULL COMMENT '微信预支付单号',
  transaction_id VARCHAR(128) NULL COMMENT '微信支付流水号',
  amount_cents INT NOT NULL COMMENT '微信充值金额（人民币分，整数）',
  scene VARCHAR(16) NOT NULL DEFAULT 'recharge' COMMENT '场景 recharge',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '微信充值支付处理状态，具体流转以 PayOrders 支付处理代码为准',
  refund_no VARCHAR(64) NULL COMMENT '退款单号',
  paid_at DATETIME(3) NULL COMMENT '支付成功时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_pay_orders_out_trade_no (out_trade_no),
  KEY idx_t_pay_orders_user (user_id),
  CONSTRAINT fk_t_pay_orders_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_pay_orders_status CHECK (status IN ('pending','paid','closed','refunding','refunded','failed')),
  CONSTRAINT chk_t_pay_orders_amount CHECK (amount_cents > 0),
  CONSTRAINT chk_t_pay_orders_scene CHECK (scene = 'recharge')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信支付充值单';

CREATE TABLE IF NOT EXISTS t_vc_delivery_outbox (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  target VARCHAR(128) NOT NULL COMMENT '接收目标',
  channel VARCHAR(16) NOT NULL COMMENT '通道 phone/email',
  code VARCHAR(16) NOT NULL COMMENT '待发送验证码明文，属于临时验证凭据',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '验证码发送任务状态，具体流转以下发队列处理代码为准',
  error VARCHAR(255) NULL COMMENT '验证码通道下发失败原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_t_vc_delivery_outbox_pending (status, created_at),
  CONSTRAINT chk_t_vc_delivery_outbox_channel CHECK (channel IN ('phone','email')),
  CONSTRAINT chk_t_vc_delivery_outbox_status CHECK (status IN ('pending','sent','blocked','failed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码真实通道下发队列';

-- ============================================================
-- 二、品类与商品域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_categories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  name VARCHAR(128) NOT NULL COMMENT '品类名称',
  icon VARCHAR(64) NULL COMMENT '图标类名字面量',
  ref_price_min DECIMAL(12,2) NULL COMMENT '参考价下限（元）',
  ref_price_max DECIMAL(12,2) NULL COMMENT '参考价上限（元）',
  region VARCHAR(64) NOT NULL DEFAULT '通用' COMMENT '传统地区文案',
  limit_qty INT NULL COMMENT '单次限购件数',
  need_permit TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要许可证',
  forbidden_note VARCHAR(512) NULL COMMENT '禁运/限制提示',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  parent_id BIGINT NULL COMMENT '父品类 → t_categories.id',
  level INT NOT NULL DEFAULT 1 COMMENT '层级 1/2/3',
  active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (id),
  KEY idx_t_categories_parent (parent_id),
  KEY idx_t_categories_level (level, sort_order),
  CONSTRAINT fk_t_categories_parent FOREIGN KEY (parent_id) REFERENCES t_categories (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三级品类树';

CREATE TABLE IF NOT EXISTS t_category_countries (
  category_id BIGINT NOT NULL COMMENT '品类 ID',
  country_code VARCHAR(8) NOT NULL COMMENT '国家/地区码',
  PRIMARY KEY (category_id, country_code),
  KEY idx_t_category_countries_code (country_code),
  CONSTRAINT fk_t_category_countries_cat FOREIGN KEY (category_id) REFERENCES t_categories (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品类-国家标签关联表（原 country text[]）';

CREATE TABLE IF NOT EXISTS t_products (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  owner_id CHAR(36) NOT NULL COMMENT '上架带货人',
  category_id BIGINT NULL COMMENT '品类 → t_categories.id',
  name VARCHAR(255) NOT NULL COMMENT '商品名',
  spec VARCHAR(255) NULL COMMENT '规格',
  region VARCHAR(64) NOT NULL DEFAULT '通用' COMMENT '产地/版本地区',
  price DECIMAL(12,2) NOT NULL COMMENT '售价（元）',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存',
  image_urls JSON NOT NULL COMMENT '商品图片地址数组（JSON）',
  note VARCHAR(1024) NULL COMMENT '说明',
  status VARCHAR(8) NOT NULL DEFAULT 'on' COMMENT '状态 on/off',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_t_products_owner (owner_id),
  KEY idx_t_products_visible (status, region),
  CONSTRAINT fk_t_products_owner FOREIGN KEY (owner_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_products_category FOREIGN KEY (category_id) REFERENCES t_categories (id) ON DELETE SET NULL,
  CONSTRAINT chk_t_products_price CHECK (price >= 0),
  CONSTRAINT chk_t_products_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='带货人在架商品';

CREATE TABLE IF NOT EXISTS t_favorites (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '收藏人',
  target_type VARCHAR(16) NOT NULL COMMENT '目标类型 product/demand',
  target_id CHAR(36) NOT NULL COMMENT '目标ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_favorites_user_target (user_id, target_type, target_id),
  KEY idx_t_favorites_user (user_id, target_type),
  CONSTRAINT fk_t_favorites_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_favorites_target_type CHECK (target_type IN ('product','demand'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏通用表';

CREATE TABLE IF NOT EXISTS t_cart_items (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '用户',
  product_id CHAR(36) NOT NULL COMMENT '商品 → t_products.id',
  qty INT NOT NULL DEFAULT 1 COMMENT '数量',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_cart_items_user_product (user_id, product_id),
  KEY idx_t_cart_items_product (product_id),
  CONSTRAINT fk_t_cart_items_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_cart_items_product FOREIGN KEY (product_id) REFERENCES t_products (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_cart_items_qty CHECK (qty >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车条目';

-- ============================================================
-- 三、行程与求购域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_trips (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  owner_id CHAR(36) NOT NULL COMMENT '发布带货人',
  destination VARCHAR(128) NOT NULL COMMENT '目的地',
  destination_flag VARCHAR(16) NULL COMMENT '目的地旗帜 emoji',
  entry_port VARCHAR(64) NULL COMMENT '入境口岸',
  transport_mode VARCHAR(16) NOT NULL COMMENT '交通方式',
  depart_date DATE NOT NULL COMMENT '出发日期',
  arrive_date DATE NULL COMMENT '抵达日期',
  return_date DATE NOT NULL COMMENT '返程/回国日期',
  capacity_qty INT NOT NULL DEFAULT 20 COMMENT '可带件数容量',
  capacity_weight_kg DECIMAL(8,2) NOT NULL DEFAULT 20.00 COMMENT '可带重量（kg）',
  fee_mode VARCHAR(16) NOT NULL DEFAULT 'percent' COMMENT '行程带货费用的计费方式',
  fee_value DECIMAL(8,2) NOT NULL DEFAULT 5.00 COMMENT '行程费率或费用值，计量方式随 fee_mode 确定',
  vendor_deposit_rate DECIMAL(4,3) NOT NULL DEFAULT 0.200 COMMENT '带货人保证金比例（小数比例，0.200 表示20%）',
  proof_urls JSON NOT NULL COMMENT '行程资质凭证图片地址数组（JSON）',
  verify_status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '行程凭证审核状态',
  verify_reason VARCHAR(255) NULL COMMENT '审核驳回原因',
  verified_at DATETIME(3) NULL COMMENT '审核通过时间',
  status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '行程当前业务状态',
  remaining_slots INT NOT NULL DEFAULT 20 COMMENT '行程剩余可接单容量（件数）',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  flight_no VARCHAR(32) NULL COMMENT '航班号',
  depart_datetime DATETIME(3) NULL COMMENT '起飞精确时刻',
  visa_proof_urls JSON NOT NULL COMMENT '签证凭证图片地址数组（JSON，属于认证材料）',
  sms_proof_urls JSON NOT NULL COMMENT '行程确认短信凭证图片地址数组（JSON）',
  boarding_snapshot_url VARCHAR(512) NULL COMMENT '登机口留证自拍 URL',
  boarding_lat DECIMAL(10,6) NULL COMMENT '登机留证纬度',
  boarding_lng DECIMAL(10,6) NULL COMMENT '登机留证经度',
  boarding_city VARCHAR(64) NULL COMMENT '登机留证城市',
  boarding_at DATETIME(3) NULL COMMENT '登机留证时间',
  boarding_risk_flags JSON NOT NULL COMMENT '登机留证校验产生的风险标记数组（JSON）',
  product_ids JSON NOT NULL COMMENT '行程挂载的在架商品 ID 数组（JSON）',
  trip_type VARCHAR(16) NOT NULL DEFAULT 'international' COMMENT '线路类型',
  direction VARCHAR(16) NOT NULL DEFAULT 'outbound' COMMENT '方向',
  origin_city VARCHAR(64) NULL COMMENT '出发城市',
  carry_mode VARCHAR(8) NOT NULL DEFAULT 'solo' COMMENT '携带模式',
  pool_slots INT NULL COMMENT '拼单池可用坑位数量',
  pool_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT '行程拼单池的开放或成团状态',
  ticket_proof_urls JSON NOT NULL COMMENT '车票或机票凭证图片地址数组（JSON）',
  arrival_snapshot_url VARCHAR(512) NULL COMMENT '到城留证快照',
  arrival_city VARCHAR(64) NULL COMMENT '到达城市',
  arrival_at DATETIME(3) NULL COMMENT '到城留证时间',
  visibility VARCHAR(8) NOT NULL DEFAULT 'public' COMMENT '行程的展示可见范围标识',
  PRIMARY KEY (id),
  KEY idx_t_trips_owner (owner_id),
  KEY idx_t_trips_visible (verify_status, status, return_date),
  KEY idx_t_trips_carry_mode (carry_mode, status),
  CONSTRAINT fk_t_trips_owner FOREIGN KEY (owner_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_trips_visibility CHECK (visibility IN ('public','hidden')),
  CONSTRAINT chk_t_trips_pool_slots CHECK (pool_slots IS NULL OR (pool_slots BETWEEN 2 AND 10)),
  CONSTRAINT chk_t_trips_carry_mode CHECK (carry_mode IN ('solo','pool')),
  CONSTRAINT chk_t_trips_pool_status CHECK (pool_status IN ('none','forming','formed','expired')),
  CONSTRAINT chk_t_trips_carry_pool CHECK ((carry_mode='solo' AND pool_slots IS NULL AND pool_status='none') OR (carry_mode='pool' AND pool_slots IS NOT NULL AND pool_status<>'none'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='带货人发布的行程';

CREATE TABLE IF NOT EXISTS t_trip_categories (
  trip_id CHAR(36) NOT NULL COMMENT '行程 ID',
  category_id BIGINT NOT NULL COMMENT '品类 ID',
  PRIMARY KEY (trip_id, category_id),
  KEY idx_t_trip_categories_cat (category_id),
  CONSTRAINT fk_t_trip_categories_trip FOREIGN KEY (trip_id) REFERENCES t_trips (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_trip_categories_cat FOREIGN KEY (category_id) REFERENCES t_categories (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程-可带品类关联表（原 category_ids bigint[]）';

CREATE TABLE IF NOT EXISTS t_demands (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  title VARCHAR(255) NOT NULL COMMENT '求购标题',
  item_desc TEXT NULL COMMENT '商品描述',
  image_urls JSON NOT NULL COMMENT '求购商品参考图片地址数组（JSON）',
  expect_price DECIMAL(12,2) NULL COMMENT '期望价（元）',
  expect_days INT NULL COMMENT '期望时效（天）',
  publisher_id CHAR(36) NOT NULL COMMENT '发布人',
  is_official TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否官方发单',
  deadline DATETIME(3) NULL COMMENT '报价截止时间',
  status VARCHAR(24) NOT NULL DEFAULT 'open' COMMENT '求购单当前发布或中标处理状态',
  awarded_quote_id CHAR(36) NULL COMMENT '中标报价 → t_quotes.id',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  pk_mode TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否 PK 盲报',
  pk_deadline DATETIME(3) NULL COMMENT 'PK 窗口截止',
  pk_result JSON NULL COMMENT 'PK 盲报报价评分快照（JSON），具体结构以 TradeRpc 和 TimeoutAndPayment 处理代码为准',
  trip_type VARCHAR(16) NOT NULL DEFAULT 'any' COMMENT '匹配线路',
  direction_pref VARCHAR(16) NOT NULL DEFAULT 'any' COMMENT '方向偏好',
  city VARCHAR(64) NULL COMMENT '需求城市',
  lat DOUBLE NULL COMMENT '需求纬度',
  lng DOUBLE NULL COMMENT '需求经度',
  tax_pref VARCHAR(32) NULL COMMENT '退税偏好',
  insurance_pref VARCHAR(16) NOT NULL DEFAULT 'unselected' COMMENT '保价意向',
  shipping_bearer VARCHAR(16) NOT NULL DEFAULT 'negotiate' COMMENT '邮费承担方',
  category_id BIGINT NULL COMMENT '所属品类',
  visibility VARCHAR(8) NOT NULL DEFAULT 'public' COMMENT '求购单的展示可见范围标识',
  matched_trip_id CHAR(36) NULL COMMENT '匹配到的行程',
  revealed_at DATETIME(3) NULL COMMENT '曝光时间',
  scheduled_at DATETIME(3) NULL COMMENT '定时发布时间',
  PRIMARY KEY (id),
  KEY idx_t_demands_status (status, is_official),
  KEY idx_t_demands_city (city),
  KEY idx_t_demands_publisher (publisher_id),
  CONSTRAINT fk_t_demands_publisher FOREIGN KEY (publisher_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_demands_category FOREIGN KEY (category_id) REFERENCES t_categories (id),
  CONSTRAINT fk_t_demands_trip FOREIGN KEY (matched_trip_id) REFERENCES t_trips (id) ON DELETE SET NULL,
  CONSTRAINT chk_t_demands_insurance CHECK (insurance_pref IN ('unselected','yes','waived')),
  CONSTRAINT chk_t_demands_shipping CHECK (shipping_bearer IN ('negotiate','buyer_pays','carrier_pays')),
  CONSTRAINT chk_t_demands_tax CHECK (tax_pref IS NULL OR tax_pref IN ('prefer_refund','accept_keep','any')),
  CONSTRAINT chk_t_demands_visibility CHECK (visibility IN ('public','hidden'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家发布的求购单';

CREATE TABLE IF NOT EXISTS t_quotes (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  demand_id CHAR(36) NOT NULL COMMENT '求购单 → t_demands.id',
  vendor_id CHAR(36) NOT NULL COMMENT '报价带货人',
  trip_id CHAR(36) NULL COMMENT '借线行程',
  price DECIMAL(12,2) NOT NULL COMMENT '报价（元）',
  promise_days INT NOT NULL COMMENT '承诺时效（天）',
  purchase_place VARCHAR(255) NULL COMMENT '采购地',
  note VARCHAR(1024) NULL COMMENT '说明',
  status VARCHAR(16) NOT NULL DEFAULT 'submitted' COMMENT '带货人报价的当前处理状态',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_t_quotes_demand_vendor (demand_id, vendor_id),
  KEY idx_t_quotes_vendor (vendor_id),
  CONSTRAINT fk_t_quotes_demand FOREIGN KEY (demand_id) REFERENCES t_demands (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_quotes_vendor FOREIGN KEY (vendor_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_quotes_trip FOREIGN KEY (trip_id) REFERENCES t_trips (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='带货人对求购单的报价';

-- Safely apply forward FK fk_t_demands_awarded_quote once.
SET @merged_ddl = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE t_demands ADD CONSTRAINT fk_t_demands_awarded_quote FOREIGN KEY (awarded_quote_id) REFERENCES t_quotes (id) ON DELETE SET NULL', 'SELECT 1')
  FROM information_schema.table_constraints
  WHERE constraint_schema = DATABASE() AND table_name = 't_demands' AND constraint_name = 'fk_t_demands_awarded_quote');
PREPARE merged_fk_stmt FROM @merged_ddl;
EXECUTE merged_fk_stmt;
DEALLOCATE PREPARE merged_fk_stmt;

CREATE TABLE IF NOT EXISTS t_publish_deposits (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '缴纳人',
  amount DECIMAL(12,2) NOT NULL COMMENT '发布求购或意向时冻结的保证金金额（元）',
  status VARCHAR(16) NOT NULL DEFAULT 'frozen' COMMENT '发布保证金的冻结或退回处理状态',
  source VARCHAR(16) NOT NULL DEFAULT 'demand' COMMENT '来源 demand/intent',
  order_id CHAR(36) NULL COMMENT '关联订单',
  reason VARCHAR(255) NULL COMMENT '说明',
  refunded_at DATETIME(3) NULL COMMENT '退回时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_t_publish_deposits_user (user_id, status),
  CONSTRAINT fk_t_publish_deposits_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布保证金';

-- ============================================================
-- 四、订单核心域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_orders (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_no VARCHAR(64) NOT NULL COMMENT '订单号（唯一）',
  trip_id CHAR(36) NULL COMMENT '关联行程',
  buyer_id CHAR(36) NOT NULL COMMENT '买家',
  vendor_id CHAR(36) NULL COMMENT '带货人',
  demand_id CHAR(36) NULL COMMENT '来源求购单',
  source VARCHAR(16) NOT NULL DEFAULT 'trip_intent' COMMENT '成单来源',
  item_name VARCHAR(255) NOT NULL COMMENT '商品名称',
  item_spec VARCHAR(255) NULL COMMENT '规格',
  quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  unit VARCHAR(16) NOT NULL DEFAULT '件' COMMENT '计量单位',
  expect_unit_price DECIMAL(12,2) NOT NULL COMMENT '期望单价（元）',
  goods_amount DECIMAL(12,2) NOT NULL COMMENT '货款总额（元）',
  service_fee DECIMAL(12,2) NOT NULL COMMENT '平台服务费金额（元）',
  buyer_deposit DECIMAL(12,2) NOT NULL COMMENT '买家冻结资金金额（元，实际组成由 agreed_json 定价版本快照确定）',
  vendor_deposit DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带货人履约保证金金额（元）',
  final_price DECIMAL(12,2) NULL COMMENT '订单最终成交金额（元）',
  ref_image_urls JSON NOT NULL COMMENT '订单商品参考图片地址数组（JSON）',
  expect_arrive_date DATE NULL COMMENT '买家期望送达日',
  promised_arrive_date DATE NULL COMMENT '带货人承诺送达日',
  contact_name VARCHAR(64) NULL COMMENT '收货联系人',
  contact_phone VARCHAR(32) NULL COMMENT '收货电话',
  address VARCHAR(512) NULL COMMENT '收货地址',
  note VARCHAR(1024) NULL COMMENT '买家备注',
  status VARCHAR(16) NOT NULL DEFAULT 'requesting' COMMENT '订单状态机',
  buyer_deposit_status VARCHAR(16) NOT NULL DEFAULT 'frozen' COMMENT '买家冻结资金的缴纳、冻结及释放状态',
  vendor_deposit_status VARCHAR(16) NOT NULL DEFAULT 'unpaid' COMMENT '带货人履约保证金的缴纳、冻结及释放状态',
  negotiation_quote DECIMAL(12,2) NULL COMMENT '带货人提出的议价金额（元）',
  negotiation_note VARCHAR(1024) NULL COMMENT '议价说明',
  negotiation_deadline DATETIME(3) NULL COMMENT '议价截止',
  intent_deadline DATETIME(3) NULL COMMENT '意向金有效期',
  vendor_deposit_deadline DATETIME(3) NULL COMMENT '补缴保证金截止',
  purchase_receipt_urls JSON NOT NULL COMMENT '采购凭证图片地址汇总数组（JSON）',
  carrier_code VARCHAR(32) NULL COMMENT '快递公司编码',
  tracking_no VARCHAR(64) NULL COMMENT '快递单号',
  shipped_at DATETIME(3) NULL COMMENT '寄出时间',
  auto_confirm_at DATETIME(3) NULL COMMENT '自动确认收货时间',
  agreed_json JSON NULL COMMENT '订单约定及定价快照（JSON），含定价版本、资金组成和保险选择等信息',
  cancel_reason_type VARCHAR(32) NULL COMMENT '取消原因类型',
  cancel_force_majeure TINYINT(1) NULL COMMENT '是否主张不可抗力',
  cancel_proof_urls JSON NOT NULL COMMENT '取消订单申请的举证图片地址数组（JSON）',
  cancel_penalty_amount DECIMAL(12,2) NULL COMMENT '取消订单裁定的违约金金额（元）',
  cancel_withdraw_used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已使用取消申请的撤回机会',
  closed_reason VARCHAR(255) NULL COMMENT '关闭原因',
  settled_at DATETIME(3) NULL COMMENT '结算完成时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  support_state VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT '订单请求平台介入的处理状态',
  support_ticket_id CHAR(36) NULL COMMENT '关联工单',
  purchase_evidence JSON NOT NULL COMMENT '采购举证对象（JSON）：receipt 小票、goods 商品、storefront 门店图片数组，可含 tax_refund_receipt 退税凭证',
  procurement_lat DECIMAL(10,6) NULL COMMENT '采购地纬度',
  procurement_lng DECIMAL(10,6) NULL COMMENT '采购地经度',
  procurement_address VARCHAR(512) NULL COMMENT '采购地地址',
  procurement_city VARCHAR(64) NULL COMMENT '采购城市',
  procurement_at DATETIME(3) NULL COMMENT '采购确认时间',
  purchase_face_verification_id CHAR(36) NULL COMMENT '采购确认刷脸记录',
  location_mismatch TINYINT(1) NOT NULL DEFAULT 0 COMMENT '采购城市与行程不符标记',
  shipping_fee DECIMAL(12,2) NULL COMMENT '国内配送邮费金额（元）',
  shipping_fee_note VARCHAR(1024) NULL COMMENT '邮费说明',
  shipping_fee_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT '国内邮费报价协商的当前状态',
  shipping_fee_deadline DATETIME(3) NULL COMMENT '邮费协商截止',
  freight_label_urls JSON NOT NULL COMMENT '快递面单图片地址数组（JSON，可能含收寄件个人信息）',
  ship_lat DECIMAL(10,6) NULL COMMENT '寄出地纬度',
  ship_lng DECIMAL(10,6) NULL COMMENT '寄出地经度',
  ship_city VARCHAR(64) NULL COMMENT '寄出城市',
  ship_address VARCHAR(512) NULL COMMENT '寄出地址',
  ship_risk_flags JSON NOT NULL COMMENT '寄出或面交校验产生的风险标记数组（JSON）',
  purchase_meta JSON NOT NULL COMMENT '采购元信息对象（JSON），含 date_str 日期条和 order_tail 订单尾号校验信息',
  purchase_risk_flags JSON NOT NULL COMMENT '采购地点与行程匹配校验产生的风险标记数组（JSON）',
  confirmed_at DATETIME(3) NULL COMMENT '买家确认收货时间',
  cancel_tier VARCHAR(16) NULL COMMENT '取消定责档位快照',
  pool_slot_no INT NULL COMMENT '拼单坑位序号',
  tax_refund_plan VARCHAR(32) NULL COMMENT '退税方案',
  tax_rate DECIMAL(5,4) NULL COMMENT '退税计算比例（小数比例，如0.08表示8%）',
  tax_refund_amount DECIMAL(12,2) NULL COMMENT '退税金额（元）',
  delivery_mode VARCHAR(16) NOT NULL DEFAULT 'ship' COMMENT '交付方式',
  meetup_code VARCHAR(8) NULL COMMENT '面交核销码',
  meetup_confirmed_json JSON NULL COMMENT '面交核销确认快照（JSON），记录买家 ID、确认时间、经纬度与城市',
  insured VARCHAR(16) NOT NULL DEFAULT 'unselected' COMMENT '订单保价或保险选择状态，实际承保以保险处理记录为准',
  insurance_premium DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '历史订单保费金额（CNY元）；机构直付版本不托管该费用，历史非零值需核对',
  declared_value DECIMAL(12,2) NULL COMMENT '商品申报价值金额（元）',
  delivery_face_verification_id CHAR(36) NULL COMMENT '交付刷脸记录',
  delivery_lat DECIMAL(10,6) NULL COMMENT '交付地点纬度',
  delivery_lng DECIMAL(10,6) NULL COMMENT '交付地点经度',
  delivery_city VARCHAR(64) NULL COMMENT '交付城市',
  delivery_at DATETIME(3) NULL COMMENT '交付时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_orders_order_no (order_no),
  KEY idx_t_orders_buyer (buyer_id),
  KEY idx_t_orders_vendor (vendor_id),
  KEY idx_t_orders_status (status),
  KEY idx_t_orders_trip (trip_id),
  KEY idx_t_orders_demand (demand_id),
  CONSTRAINT fk_t_orders_buyer FOREIGN KEY (buyer_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_orders_vendor FOREIGN KEY (vendor_id) REFERENCES t_profiles (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_orders_trip FOREIGN KEY (trip_id) REFERENCES t_trips (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_orders_demand FOREIGN KEY (demand_id) REFERENCES t_demands (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_orders_face FOREIGN KEY (purchase_face_verification_id) REFERENCES t_face_verifications (id) ON DELETE SET NULL,
  CONSTRAINT chk_t_orders_insured CHECK (insured IN ('unselected','yes','waived')),
  CONSTRAINT chk_t_orders_delivery_mode CHECK (delivery_mode IN ('ship','meetup')),
  CONSTRAINT chk_t_orders_tax_plan CHECK (tax_refund_plan IS NULL OR tax_refund_plan IN ('refund_to_buyer','vendor_keeps'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS t_deposits (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_id CHAR(36) NOT NULL COMMENT '订单 → t_orders.id',
  payer_id CHAR(36) NOT NULL COMMENT '缴纳人',
  party_role VARCHAR(16) NOT NULL COMMENT '缴纳方角色 buyer/vendor',
  kind VARCHAR(16) NOT NULL COMMENT '保证金类型',
  amount DECIMAL(12,2) NOT NULL COMMENT '金额（元）',
  status VARCHAR(16) NOT NULL DEFAULT 'frozen' COMMENT '状态',
  payee_id CHAR(36) NULL COMMENT '收款方',
  reason VARCHAR(255) NULL COMMENT '变动原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_t_deposits_order (order_id),
  KEY idx_t_deposits_payer (payer_id),
  CONSTRAINT fk_t_deposits_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_deposits_payer FOREIGN KEY (payer_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_deposits_payee FOREIGN KEY (payee_id) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单保证金流水台账';

CREATE TABLE IF NOT EXISTS t_order_logistics (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_id CHAR(36) NOT NULL COMMENT '订单',
  node VARCHAR(32) NOT NULL COMMENT '物流节点',
  operator_id CHAR(36) NULL COMMENT '操作人',
  operator_role VARCHAR(16) NOT NULL DEFAULT 'vendor' COMMENT '操作角色',
  detail VARCHAR(1024) NULL COMMENT '节点说明',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录时间',
  PRIMARY KEY (id),
  KEY idx_t_order_logistics_order (order_id),
  CONSTRAINT fk_t_order_logistics_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_order_logistics_operator FOREIGN KEY (operator_id) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单物流节点轨迹';

CREATE TABLE IF NOT EXISTS t_cancel_requests (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_id CHAR(36) NOT NULL COMMENT '订单',
  applicant_id CHAR(36) NOT NULL COMMENT '申请人',
  applicant_role VARCHAR(16) NOT NULL COMMENT '申请角色 buyer/vendor',
  reason_type VARCHAR(32) NOT NULL COMMENT '原因类型',
  reason_detail VARCHAR(1024) NULL COMMENT '原因详情',
  proof_urls JSON NOT NULL COMMENT '取消申请举证图片地址数组（JSON）',
  claim_force_majeure TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主张不可抗力',
  mutual_plan JSON NULL COMMENT '双方协商的取消处理方案（JSON），具体结构以取消申请处理代码为准',
  status VARCHAR(16) NOT NULL DEFAULT 'reviewing' COMMENT '取消申请当前受理或裁定状态',
  verdict VARCHAR(16) NULL COMMENT '定责结论',
  penalty_amount DECIMAL(12,2) NULL COMMENT '取消定责裁定违约金金额（元）',
  verdict_reason VARCHAR(1024) NULL COMMENT '裁定理由',
  withdrawn TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否撤回',
  reviewed_by CHAR(36) NULL COMMENT '核责运营',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  resolved_at DATETIME(3) NULL COMMENT '裁定时间',
  PRIMARY KEY (id),
  KEY idx_t_cancel_requests_order (order_id),
  KEY idx_t_cancel_requests_status (status),
  CONSTRAINT fk_t_cancel_requests_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_cancel_requests_applicant FOREIGN KEY (applicant_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_cancel_requests_reviewer FOREIGN KEY (reviewed_by) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='取消申请与定责';

-- Safely apply forward FK fk_t_publish_deposits_order once.
SET @merged_ddl = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE t_publish_deposits ADD CONSTRAINT fk_t_publish_deposits_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL', 'SELECT 1')
  FROM information_schema.table_constraints
  WHERE constraint_schema = DATABASE() AND table_name = 't_publish_deposits' AND constraint_name = 'fk_t_publish_deposits_order');
PREPARE merged_fk_stmt FROM @merged_ddl;
EXECUTE merged_fk_stmt;
DEALLOCATE PREPARE merged_fk_stmt;

-- ============================================================
-- 五、交易保障域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_disputes (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_id CHAR(36) NOT NULL COMMENT '订单',
  initiator_id CHAR(36) NOT NULL COMMENT '发起方',
  respondent_id CHAR(36) NULL COMMENT '被诉方',
  type VARCHAR(32) NOT NULL COMMENT '纠纷类型',
  expect_resolution VARCHAR(64) NOT NULL COMMENT '期望处理方式',
  description TEXT NOT NULL COMMENT '问题描述',
  evidence_urls JSON NOT NULL COMMENT '纠纷发起方举证图片地址数组（JSON）',
  respondent_evidence_urls JSON NOT NULL COMMENT '纠纷被诉方举证图片地址数组（JSON）',
  respondent_note VARCHAR(1024) NULL COMMENT '被诉方说明',
  stage VARCHAR(24) NOT NULL DEFAULT 'negotiating' COMMENT '纠纷当前协商或裁决处理阶段',
  round INT NOT NULL DEFAULT 1 COMMENT '纠纷当前协商轮次数',
  verdict VARCHAR(64) NULL COMMENT '裁决结果',
  refund_amount DECIMAL(12,2) NULL COMMENT '纠纷裁定退款金额（元）',
  deposit_owner VARCHAR(16) NULL COMMENT '保证金归属判定',
  verdict_reason VARCHAR(1024) NULL COMMENT '裁决理由',
  reviewed_by CHAR(36) NULL COMMENT '裁决运营',
  reopened_used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已用过一次复议',
  review_appeal TEXT NULL COMMENT '复议申诉内容',
  review_result VARCHAR(64) NULL COMMENT '复议结果',
  negotiation_deadline DATETIME(3) NULL COMMENT '协商截止',
  resolved_at DATETIME(3) NULL COMMENT '解决时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_t_disputes_order (order_id),
  KEY idx_t_disputes_stage (stage),
  CONSTRAINT fk_t_disputes_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_disputes_initiator FOREIGN KEY (initiator_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_disputes_respondent FOREIGN KEY (respondent_id) REFERENCES t_profiles (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_disputes_reviewer FOREIGN KEY (reviewed_by) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纠纷工单';

CREATE TABLE IF NOT EXISTS t_reviews (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_id CHAR(36) NOT NULL COMMENT '订单',
  reviewer_id CHAR(36) NOT NULL COMMENT '评价人',
  target_id CHAR(36) NOT NULL COMMENT '被评价人',
  stars INT NOT NULL DEFAULT 5 COMMENT '星级 1-5',
  tags JSON NOT NULL COMMENT '订单评价标签文本数组（JSON）',
  content TEXT NULL COMMENT '文字评价',
  hidden TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_reviews_order_reviewer (order_id, reviewer_id),
  KEY idx_t_reviews_target (target_id),
  CONSTRAINT fk_t_reviews_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_reviews_target FOREIGN KEY (target_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_reviews_stars CHECK (stars BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单完成后的互评';

CREATE TABLE IF NOT EXISTS t_balance_ledger (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '入账用户',
  kind VARCHAR(16) NOT NULL COMMENT '流水类型',
  amount DECIMAL(12,2) NOT NULL COMMENT '变动金额（±，元）',
  balance_after DECIMAL(12,2) NULL COMMENT '该笔流水落账后的钱包余额快照（元）',
  order_id CHAR(36) NULL COMMENT '关联订单',
  note VARCHAR(255) NULL COMMENT '备注',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '落账时间',
  PRIMARY KEY (id),
  KEY idx_t_balance_ledger_user (user_id, created_at),
  CONSTRAINT fk_t_balance_ledger_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_balance_ledger_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包余额总账';

CREATE TABLE IF NOT EXISTS t_invoices (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  order_id CHAR(36) NOT NULL COMMENT '订单（每单一票）',
  user_id CHAR(36) NOT NULL COMMENT '申请人',
  title_type VARCHAR(16) NOT NULL DEFAULT 'personal' COMMENT '抬头类型',
  title_name VARCHAR(128) NOT NULL COMMENT '抬头名称',
  tax_no VARCHAR(64) NULL COMMENT '纳税人识别号',
  invoice_kind VARCHAR(32) NOT NULL DEFAULT 'electronic_general' COMMENT '发票种类',
  amount DECIMAL(12,2) NOT NULL COMMENT '申请开具发票的金额（元）',
  email VARCHAR(128) NOT NULL COMMENT '接收邮箱',
  remark VARCHAR(1024) NULL COMMENT '备注',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '电子发票申请审核或开具状态',
  invoice_no VARCHAR(64) NULL COMMENT '发票号',
  file_url VARCHAR(512) NULL COMMENT '发票文件 URL',
  issued_at DATETIME(3) NULL COMMENT '开具时间',
  reject_reason VARCHAR(255) NULL COMMENT '驳回原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '申请时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_invoices_order (order_id),
  KEY idx_t_invoices_user (user_id, created_at),
  KEY idx_t_invoices_status (status, created_at),
  CONSTRAINT fk_t_invoices_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_invoices_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_invoices_title CHECK (title_type IN ('personal','company')),
  CONSTRAINT chk_t_invoices_status CHECK (status IN ('pending','issued','rejected')),
  CONSTRAINT chk_t_invoices_kind CHECK (invoice_kind IN ('electronic_general','vat_special'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子发票申请与开具';

CREATE TABLE IF NOT EXISTS t_coupons (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '持有人',
  face_value DECIMAL(10,2) NOT NULL COMMENT '面值（元）',
  points_cost INT NOT NULL COMMENT '积分兑换分值',
  status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '优惠券当前可用、核销或过期状态',
  used_order_id CHAR(36) NULL COMMENT '核销订单',
  expire_at DATETIME(3) NOT NULL COMMENT '过期时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_t_coupons_user (user_id, status),
  CONSTRAINT fk_t_coupons_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_coupons_order FOREIGN KEY (used_order_id) REFERENCES t_orders (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券/积分兑换券';

-- ============================================================
-- 六、分销激励域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_invite_codes (
  code VARCHAR(32) NOT NULL COMMENT '邀请码（主键）',
  note VARCHAR(255) NULL COMMENT '备注',
  used_by CHAR(36) NULL COMMENT '使用者 → t_profiles.id',
  used_at DATETIME(3) NULL COMMENT '使用时间',
  created_by CHAR(36) NULL COMMENT '创建运营 → t_profiles.id',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (code),
  KEY idx_t_invite_codes_used_by (used_by),
  CONSTRAINT fk_t_invite_codes_used_by FOREIGN KEY (used_by) REFERENCES t_profiles (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_invite_codes_created_by FOREIGN KEY (created_by) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台邀请码池';

-- 注意：order_id 是外键列，不参与生成列；部分唯一索引改为普通唯一索引 + 触发器
CREATE TABLE IF NOT EXISTS t_reward_ledger (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  inviter_id CHAR(36) NOT NULL COMMENT '受益邀请人',
  invitee_id CHAR(36) NOT NULL COMMENT '被邀请人',
  order_id CHAR(36) NULL COMMENT '计佣订单（首单奖励为 NULL）',
  level SMALLINT NOT NULL COMMENT '层级 1-4',
  event_type VARCHAR(32) NOT NULL COMMENT '事件类型',
  reward_kind VARCHAR(16) NOT NULL DEFAULT 'points' COMMENT '奖励种类',
  amount INT NOT NULL COMMENT '数额（cny 存分；points 存分值）',
  status VARCHAR(16) NOT NULL DEFAULT 'settled' COMMENT '奖励记录的结算或撤销状态',
  note VARCHAR(255) NULL COMMENT '说明',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  rate DECIMAL(6,4) NULL COMMENT '团队提成计算比例快照（小数比例）',
  base_amount DECIMAL(12,2) NULL COMMENT '计算团队提成时采用的平台服务费基数（元）',
  currency VARCHAR(8) NOT NULL DEFAULT 'points' COMMENT '币种 points/cny',
  settled_to_balance TINYINT(1) NOT NULL DEFAULT 0 COMMENT '该笔现金奖励是否已计入用户钱包余额',
  revoked_at DATETIME(3) NULL COMMENT '撤销时间',
  revoked_reason VARCHAR(255) NULL COMMENT '撤销原因',
  PRIMARY KEY (id),
  KEY idx_t_reward_ledger_inviter (inviter_id, created_at),
  KEY idx_t_reward_ledger_event (inviter_id, event_type, created_at),
  KEY idx_t_reward_ledger_order (order_id),
  CONSTRAINT fk_t_reward_ledger_inviter FOREIGN KEY (inviter_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_reward_ledger_invitee FOREIGN KEY (invitee_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_reward_ledger_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL,
  CONSTRAINT chk_t_reward_ledger_level CHECK (level BETWEEN 1 AND 4),
  CONSTRAINT chk_t_reward_ledger_currency CHECK (currency IN ('points','cny'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销奖励台账（双币种）';

CREATE TABLE IF NOT EXISTS t_score_logs (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '用户',
  order_id CHAR(36) NULL COMMENT '关联订单',
  event VARCHAR(64) NOT NULL COMMENT '触发事件',
  delta INT NOT NULL COMMENT '分值增减',
  score_after INT NOT NULL COMMENT '变动后总分',
  note VARCHAR(255) NULL COMMENT '说明',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_t_score_logs_user (user_id),
  CONSTRAINT fk_t_score_logs_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_score_logs_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='履约分变动日志';

-- ============================================================
-- 七、消息与客服域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_notifications (
  id CHAR(36) NOT NULL COMMENT 'UUID 主键',
  user_id CHAR(36) NOT NULL COMMENT '接收人',
  kind VARCHAR(32) NOT NULL COMMENT '通知类型',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  body TEXT NULL COMMENT '正文',
  order_id CHAR(36) NULL COMMENT '关联订单',
  read_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '已读',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_t_notifications_user (user_id, read_flag),
  CONSTRAINT fk_t_notifications_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_notifications_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';

CREATE TABLE IF NOT EXISTS t_notify_outbox (
  id CHAR(36) NOT NULL COMMENT '消息队列记录UUID',
  user_id CHAR(36) NOT NULL COMMENT '接收用户ID',
  order_id CHAR(36) NULL COMMENT '关联订单ID',
  kind VARCHAR(32) NOT NULL COMMENT '订阅消息模板类型',
  title VARCHAR(255) NOT NULL COMMENT '消息标题',
  body TEXT NULL COMMENT '消息正文',
  channel VARCHAR(32) NOT NULL DEFAULT 'wechat_subscribe' COMMENT '消息发送通道，默认wechat_subscribe',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '消息发送状态，默认pending待发送',
  reason VARCHAR(255) NULL COMMENT '发送失败原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息入队时间',
  sent_at DATETIME(3) NULL COMMENT '消息发送时间',
  PRIMARY KEY (id),
  KEY idx_t_notify_outbox_pending (status, created_at),
  CONSTRAINT fk_t_notify_outbox_user FOREIGN KEY (user_id) REFERENCES t_profiles (id),
  CONSTRAINT chk_t_notify_outbox_status CHECK (status IN ('pending','sent','failed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信订阅消息待发送队列及发送结果';

CREATE TABLE IF NOT EXISTS t_messages (
  id CHAR(36) NOT NULL COMMENT '订单留言UUID',
  order_id CHAR(36) NOT NULL COMMENT '所属订单ID',
  sender_id CHAR(36) NOT NULL COMMENT '发送用户ID',
  content TEXT NOT NULL COMMENT '留言正文',
  image_urls JSON NOT NULL COMMENT '留言图片URL列表，JSON数组',
  system_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统消息：1是、0否',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '留言创建时间',
  sender_role VARCHAR(16) NOT NULL DEFAULT 'normal' COMMENT '留言发送角色，默认normal',
  PRIMARY KEY (id),
  KEY idx_t_messages_order (order_id),
  CONSTRAINT fk_t_messages_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_messages_sender FOREIGN KEY (sender_id) REFERENCES t_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单参与方会话留言及图片消息';

-- 注意：user_id / peer_id / order_id 是外键列，不参与生成列；唯一性由触发器维护
CREATE TABLE IF NOT EXISTS t_support_threads (
  id CHAR(36) NOT NULL COMMENT '会话线程UUID',
  kind VARCHAR(16) NOT NULL COMMENT '会话类型：ai人工智能、human人工客服、peer用户私聊',
  user_id CHAR(36) NOT NULL COMMENT '会话发起用户ID',
  peer_id CHAR(36) NULL COMMENT '私聊对端用户ID，适用于peer会话',
  order_id CHAR(36) NULL COMMENT '关联订单ID',
  status VARCHAR(16) NOT NULL DEFAULT 'open' COMMENT '线程状态，默认open',
  last_message_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最近一条消息时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '会话创建时间',
  PRIMARY KEY (id),
  KEY idx_t_support_threads_user (user_id, last_message_at),
  KEY idx_t_support_threads_peer_lookup (user_id, peer_id, order_id),
  CONSTRAINT fk_t_support_threads_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_support_threads_peer FOREIGN KEY (peer_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_support_threads_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL,
  CONSTRAINT chk_t_support_threads_status CHECK (status IN ('open','closed')),
  CONSTRAINT chk_t_support_threads_kind CHECK (kind IN ('ai','human','peer'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服、AI及好友私聊会话线程';

CREATE TABLE IF NOT EXISTS t_support_tickets (
  id CHAR(36) NOT NULL COMMENT '客服工单UUID',
  user_id CHAR(36) NOT NULL COMMENT '工单发起用户ID',
  order_id CHAR(36) NULL COMMENT '关联订单ID',
  topic VARCHAR(255) NOT NULL COMMENT '工单主题',
  content TEXT NOT NULL COMMENT '工单问题内容',
  status VARCHAR(16) NOT NULL DEFAULT 'open' COMMENT '工单处理状态，默认open',
  handler_id CHAR(36) NULL COMMENT '处理客服用户ID',
  reply TEXT NULL COMMENT '客服回复内容',
  last_reply_at DATETIME(3) NULL COMMENT '最近回复时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '工单创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '工单更新时间',
  PRIMARY KEY (id),
  KEY idx_t_support_tickets_status (status, created_at),
  KEY idx_t_support_tickets_user (user_id, created_at),
  CONSTRAINT fk_t_support_tickets_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_support_tickets_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_support_tickets_handler FOREIGN KEY (handler_id) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版客服工单及客服处理回复';

CREATE TABLE IF NOT EXISTS t_support_messages (
  id CHAR(36) NOT NULL COMMENT '会话消息UUID',
  thread_id CHAR(36) NOT NULL COMMENT '所属会话线程ID，关联t_support_threads.id',
  sender_id CHAR(36) NULL COMMENT '发送用户ID，系统消息可为空',
  sender_role VARCHAR(16) NOT NULL DEFAULT 'user' COMMENT '消息发送角色，默认user',
  content TEXT NOT NULL COMMENT '消息正文',
  image_urls JSON NOT NULL COMMENT '消息图片URL列表，JSON数组',
  read_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：1已读、0未读',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息创建时间',
  PRIMARY KEY (id),
  KEY idx_t_support_messages_thread (thread_id, created_at),
  CONSTRAINT fk_t_support_messages_thread FOREIGN KEY (thread_id) REFERENCES t_support_threads (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_support_messages_sender FOREIGN KEY (sender_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_support_messages_role CHECK (sender_role IN ('user','ai','agent','system'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服及私聊会话线程中的消息';

-- 注意：user_id 是外键列，不参与生成列；部分索引语义改为普通索引 + 触发器
CREATE TABLE IF NOT EXISTS t_chat_violations (
  id CHAR(36) NOT NULL COMMENT '违规记录UUID',
  user_id CHAR(36) NOT NULL COMMENT '命中违规规则的用户ID',
  code VARCHAR(64) NOT NULL COMMENT '命中的防引流规则编码',
  snippet VARCHAR(512) NULL COMMENT '命中规则的聊天文本片段',
  reviewed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成人工复核：1是、0否',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '违规留痕时间',
  PRIMARY KEY (id),
  KEY idx_t_chat_violations_user (user_id, created_at),
  KEY idx_t_chat_violations_open (reviewed, created_at),
  CONSTRAINT fk_t_chat_violations_user FOREIGN KEY (user_id) REFERENCES t_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天防引流规则命中及人工复核记录';

-- ============================================================
-- 八、通话与隐私域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_call_records (
  id CHAR(36) NOT NULL COMMENT '通话记录UUID',
  order_id CHAR(36) NULL COMMENT '关联订单ID',
  caller_id CHAR(36) NOT NULL COMMENT '主叫用户ID',
  callee_id CHAR(36) NOT NULL COMMENT '被叫用户ID',
  status VARCHAR(16) NOT NULL DEFAULT 'ringing' COMMENT '通话状态，默认ringing响铃中',
  room_id VARCHAR(64) NULL COMMENT 'RTC通话房间编号',
  started_at DATETIME(3) NULL COMMENT '通话接通时间',
  ended_at DATETIME(3) NULL COMMENT '通话挂断时间',
  duration_sec INT NOT NULL DEFAULT 0 COMMENT '通话时长，单位秒',
  recording_url VARCHAR(512) NULL COMMENT '录音存档URL',
  transcript_flags JSON NOT NULL COMMENT '语音转写风险词标记列表，JSON数组',
  consent_confirmed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '双方是否确认录音知情：1是、0否',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '通话记录创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '通话记录更新时间',
  PRIMARY KEY (id),
  KEY idx_t_call_records_parties (caller_id, callee_id, created_at),
  KEY idx_t_call_records_order (order_id),
  CONSTRAINT fk_t_call_records_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE SET NULL,
  CONSTRAINT fk_t_call_records_caller FOREIGN KEY (caller_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_call_records_callee FOREIGN KEY (callee_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT chk_t_call_records_status CHECK (status IN ('ringing','accepted','ended','rejected','missed','cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网络语音通话及录音知情确认记录';

CREATE TABLE IF NOT EXISTS t_privacy_numbers (
  virtual_number VARCHAR(32) NOT NULL COMMENT '虚拟中间号码，主键',
  pool_code VARCHAR(32) NOT NULL DEFAULT 'default' COMMENT '所属号码池编码，默认default',
  status VARCHAR(16) NOT NULL DEFAULT 'idle' COMMENT '号码资源状态，默认idle空闲',
  remark VARCHAR(255) NULL COMMENT '号码资源备注',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '号码入池时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '号码信息更新时间',
  PRIMARY KEY (virtual_number),
  KEY idx_t_privacy_numbers_status (status, pool_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐私通话虚拟中间号码资源池';

-- 注意：order_id 是外键列，不参与生成列；活跃绑定唯一性由触发器维护
CREATE TABLE IF NOT EXISTS t_privacy_bindings (
  id CHAR(36) NOT NULL COMMENT '虚拟号绑定记录UUID',
  order_id CHAR(36) NOT NULL COMMENT '关联订单ID',
  virtual_number VARCHAR(32) NOT NULL COMMENT '绑定占用的虚拟中间号码',
  buyer_phone_masked VARCHAR(32) NULL COMMENT '脱敏后的买家电话号码，不存储完整明文号码',
  vendor_phone_masked VARCHAR(32) NULL COMMENT '脱敏后的带货人电话号码，不存储完整明文号码',
  aliyun_sub_id VARCHAR(64) NULL COMMENT '运营商返回的号码绑定标识',
  expire_at DATETIME(3) NOT NULL COMMENT '号码绑定到期时间',
  status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '绑定状态，默认active有效',
  release_reason VARCHAR(255) NULL COMMENT '号码绑定释放原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '绑定创建时间',
  released_at DATETIME(3) NULL COMMENT '绑定实际释放时间',
  PRIMARY KEY (id),
  KEY idx_t_privacy_bindings_active (order_id, status),
  KEY idx_t_privacy_bindings_expire (status, expire_at),
  CONSTRAINT fk_t_privacy_bindings_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_privacy_bindings_number FOREIGN KEY (virtual_number) REFERENCES t_privacy_numbers (virtual_number) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单双方隐私通话虚拟号码绑定及释放记录';

CREATE TABLE IF NOT EXISTS t_call_logs (
  id CHAR(36) NOT NULL COMMENT '虚拟号拨打记录UUID',
  order_id CHAR(36) NOT NULL COMMENT '关联订单ID',
  binding_id CHAR(36) NULL COMMENT '关联虚拟号绑定记录ID',
  caller_id CHAR(36) NOT NULL COMMENT '主叫用户ID',
  caller_role VARCHAR(16) NOT NULL COMMENT '主叫方在订单中的角色',
  peer_role VARCHAR(16) NOT NULL COMMENT '被叫方在订单中的角色',
  virtual_number VARCHAR(32) NOT NULL COMMENT '本次拨打使用的虚拟号码',
  result VARCHAR(16) NOT NULL DEFAULT 'dialed' COMMENT '拨打结果，默认dialed已发起拨打',
  duration_sec INT NULL COMMENT '通话时长，单位秒',
  note VARCHAR(255) NULL COMMENT '拨打记录备注',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '拨打记录创建时间',
  PRIMARY KEY (id),
  KEY idx_t_call_logs_order (order_id, created_at),
  CONSTRAINT fk_t_call_logs_order FOREIGN KEY (order_id) REFERENCES t_orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_call_logs_caller FOREIGN KEY (caller_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_call_logs_binding FOREIGN KEY (binding_id) REFERENCES t_privacy_bindings (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通过订单隐私虚拟号发起的拨打记录';

-- ============================================================
-- 九、平台治理域
-- ============================================================

CREATE TABLE IF NOT EXISTS t_admin_logs (
  id CHAR(36) NOT NULL COMMENT '后台操作记录UUID',
  operator_id CHAR(36) NULL COMMENT '执行操作的运营用户ID',
  action VARCHAR(64) NOT NULL COMMENT '后台操作类型编码',
  target_type VARCHAR(32) NULL COMMENT '被操作对象类型',
  target_id VARCHAR(64) NULL COMMENT '被操作对象标识',
  detail TEXT NULL COMMENT '后台操作详情',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作留痕时间',
  PRIMARY KEY (id),
  KEY idx_t_admin_logs_operator (operator_id),
  CONSTRAINT fk_t_admin_logs_operator FOREIGN KEY (operator_id) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营后台操作审计留痕';

CREATE TABLE IF NOT EXISTS t_legal_docs (
  id CHAR(36) NOT NULL COMMENT '法律文档UUID',
  doc_key VARCHAR(64) NOT NULL COMMENT '文档唯一业务键',
  title VARCHAR(255) NOT NULL COMMENT '文档标题',
  body_md MEDIUMTEXT NOT NULL COMMENT 'Markdown格式文档正文',
  version VARCHAR(16) NOT NULL DEFAULT 'v1.0' COMMENT '文档版本号，默认v1.0',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '文档最近更新时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '文档创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t_legal_docs_doc_key (doc_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='法律协议及说明文档的正文和版本';

CREATE TABLE IF NOT EXISTS t_join_applications (
  id CHAR(36) NOT NULL COMMENT '加入申请UUID',
  operator_id CHAR(36) NOT NULL COMMENT '提交申请的运营用户ID',
  target_username VARCHAR(64) NOT NULL COMMENT '申请目标账号用户名',
  target_user_id CHAR(36) NULL COMMENT '申请目标用户ID',
  note VARCHAR(1024) NULL COMMENT '加入申请说明',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '申请处理状态，默认pending待处理',
  responded_at DATETIME(3) NULL COMMENT '申请处理时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '申请创建时间',
  PRIMARY KEY (id),
  KEY idx_t_join_applications_target (target_user_id, status),
  CONSTRAINT fk_t_join_applications_operator FOREIGN KEY (operator_id) REFERENCES t_profiles (id) ON DELETE CASCADE,
  CONSTRAINT fk_t_join_applications_target FOREIGN KEY (target_user_id) REFERENCES t_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营账号加入申请与处理记录';

-- ============================================================
-- 十一、社交
-- ============================================================

CREATE TABLE IF NOT EXISTS t_social_posts (
 id CHAR(36) PRIMARY KEY COMMENT '社区动态UUID', author_id CHAR(36) NOT NULL COMMENT '动态发布用户ID',caption VARCHAR(4000) NOT NULL COMMENT '动态文字内容',
 media_kind VARCHAR(12) NOT NULL DEFAULT 'text' COMMENT '媒体类型，默认text纯文字',media_url VARCHAR(800) COMMENT '动态媒体资源URL',poster_url VARCHAR(800) COMMENT '动态媒体封面URL',
 status VARCHAR(24) NOT NULL DEFAULT 'pending_review' COMMENT '内容审核状态：pending_review待审核、approved通过、rejected拒绝',reviewer_id CHAR(36) COMMENT '审核运营用户ID',reviewed_at DATETIME COMMENT '审核完成时间',
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '动态创建时间',
 INDEX idx_t_social_posts_public(status,created_at),INDEX idx_t_social_posts_author(author_id,created_at),
 CONSTRAINT chk_t_social_type CHECK(media_kind IN ('text','image','video')),
 CONSTRAINT chk_t_social_status CHECK(status IN ('pending_review','approved','rejected')),
 FOREIGN KEY(author_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门户社区动态内容及审核记录';

-- ============================================================
-- 十二、客服会话
-- ============================================================
CREATE TABLE IF NOT EXISTS t_support_conversations (
 id CHAR(36) PRIMARY KEY COMMENT '客服接待会话UUID',user_id CHAR(36) NOT NULL COMMENT '发起会话的用户ID',
 status VARCHAR(16) NOT NULL DEFAULT 'ai' COMMENT '接待状态：ai智能客服、waiting等待人工、human人工接待',assigned_agent_id CHAR(36) COMMENT '已分配人工客服用户ID',
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接待会话创建时间',
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '接待会话更新时间',
 INDEX idx_t_support_user(user_id,status,created_at),INDEX idx_t_support_queue(status,created_at),
 CONSTRAINT chk_t_support_state CHECK(status IN ('ai','waiting','human','closed')),
 FOREIGN KEY(user_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与AI及人工客服的接待会话';
-- ============================================================
-- 十三、客服信息
-- ============================================================
CREATE TABLE IF NOT EXISTS t_support_conversation_messages (
 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增消息主键',conversation_id CHAR(36) NOT NULL COMMENT '所属客服接待会话ID',
 sender_type VARCHAR(16) NOT NULL COMMENT '消息发送方类型',sender_id CHAR(36) COMMENT '发送方用户ID，非用户发送时可为空',content VARCHAR(5000) NOT NULL COMMENT '客服会话消息正文',
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
 INDEX idx_t_support_msg(conversation_id,id),
 CONSTRAINT chk_t_support_sender CHECK(sender_type IN ('user','ai','agent')),
 FOREIGN KEY(conversation_id) REFERENCES t_support_conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服接待会话的往来消息';
-- ============================================================
-- 十四、
-- ============================================================
CREATE TABLE IF NOT EXISTS t_portal_demand_reviews (
 demand_id CHAR(36) PRIMARY KEY COMMENT '待审核需求ID，主键',author_id CHAR(36) NOT NULL COMMENT '需求发布用户ID',
 status VARCHAR(24) NOT NULL DEFAULT 'pending_review' COMMENT '需求审核状态：pending_review待审核、approved通过、rejected拒绝',reviewer_id CHAR(36) COMMENT '审核运营用户ID',reviewed_at DATETIME COMMENT '需求审核完成时间',
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核记录创建时间',
 INDEX idx_t_demand_reviews(status,created_at),
 CONSTRAINT chk_t_demand_review_state CHECK(status IN ('pending_review','approved','rejected')),
 FOREIGN KEY(demand_id) REFERENCES t_demands(id) ON DELETE CASCADE,
 FOREIGN KEY(author_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门户需求发布内容审核记录';

CREATE TABLE IF NOT EXISTS t_user_preferences (
 user_id CHAR(36) PRIMARY KEY COMMENT '用户ID，主键', locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN' COMMENT '界面语言地区代码，默认zh-CN', country_code CHAR(2) NOT NULL DEFAULT 'CN' COMMENT '国家或地区代码，默认CN', currency_code CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '偏好币种代码，默认CNY', time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '用户时区标识，默认Asia/Shanghai'
) COMMENT='用户语言、地区、币种及时区偏好';
CREATE TABLE IF NOT EXISTS t_revoked_tokens (
 token_hash CHAR(64) PRIMARY KEY COMMENT '已撤销令牌的摘要，主键；不保存令牌明文', expires_at DATETIME NOT NULL COMMENT '令牌原定过期时间', revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '令牌撤销时间', INDEX idx_t_revoked_expiry(expires_at)
) COMMENT='已撤销认证令牌摘要及失效时间';
CREATE TABLE IF NOT EXISTS t_account_deletion_requests (
 id CHAR(36) PRIMARY KEY COMMENT '账号注销申请UUID', user_id CHAR(36) NOT NULL COMMENT '申请注销的用户ID', reason VARCHAR(1000) COMMENT '用户填写的注销原因', status VARCHAR(24) NOT NULL DEFAULT 'submitted' COMMENT '注销申请处理状态，默认submitted已提交', operator_note VARCHAR(1000) COMMENT '运营处理备注', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注销申请创建时间', updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '注销申请更新时间'
) COMMENT='用户账号注销申请及运营处理记录';
-- A product is not eligible for cross-border sale until a compliance reviewer
-- records its HS code, approved mode, port and official-list evidence.
CREATE TABLE IF NOT EXISTS t_cbec_product_reviews (
 product_id CHAR(36) PRIMARY KEY COMMENT '被审核商品ID，主键', hs_code VARCHAR(20) NOT NULL COMMENT '商品海关HS编码', import_mode VARCHAR(10) NOT NULL COMMENT '商品进口模式代码',
 port_code VARCHAR(40) NOT NULL COMMENT '进口口岸代码', list_version VARCHAR(80) NOT NULL COMMENT '审核所依据清单的版本标识', official_source VARCHAR(800) NOT NULL COMMENT '审核所依据的官方信息来源',
 decision VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '商品审核结论，默认pending待审核', reviewed_by CHAR(36) COMMENT '审核人员用户ID', reviewed_at DATETIME COMMENT '审核时间',
 review_note VARCHAR(1000) COMMENT '审核说明', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核记录创建时间',
 CONSTRAINT chk_cbec_mode CHECK(import_mode IN ('1210','9610')),
 CONSTRAINT chk_cbec_decision CHECK(decision IN ('pending','approved','rejected'))
) COMMENT='跨境电商商品合规资料及审核结论';
CREATE TABLE IF NOT EXISTS t_return_cases (
 id CHAR(36) PRIMARY KEY COMMENT '退货案件UUID', order_id CHAR(36) NOT NULL COMMENT '关联订单ID', applicant_id CHAR(36) NOT NULL COMMENT '退货申请用户ID',
 reason VARCHAR(1000) NOT NULL COMMENT '退货申请原因', status VARCHAR(30) NOT NULL DEFAULT 'requested' COMMENT '退货处理状态，默认requested已申请',
 customs_application_at DATETIME COMMENT '实际向海关提交退货申请时间', goods_arrived_at DATETIME COMMENT '退货货物实际到达时间',
 customs_application_deadline DATETIME NOT NULL COMMENT '海关退货申请截止时间', goods_arrival_deadline DATETIME NOT NULL COMMENT '退货货物到达截止时间',
 tax_benefit_eligible BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否符合税收优惠条件：1是、0否',
 customs_reference VARCHAR(100) COMMENT '海关退货业务参考编号', destination_facility VARCHAR(200) COMMENT '退货目的监管设施名称', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '退货案件创建时间',
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '退货案件更新时间'
) COMMENT='跨境退货案件、申报及货物到达时限记录';
CREATE TABLE IF NOT EXISTS t_after_sales_cases (
 id CHAR(36) PRIMARY KEY COMMENT '售后案件UUID', order_id CHAR(36) NOT NULL COMMENT '关联订单ID', applicant_id CHAR(36) NOT NULL COMMENT '售后申请用户ID',
 case_type VARCHAR(32) NOT NULL COMMENT '售后案件类型编码', reason VARCHAR(2000) NOT NULL COMMENT '售后申请原因', evidence_upload_ids JSON COMMENT '售后证据上传记录ID列表，JSON数组',
 status VARCHAR(24) NOT NULL DEFAULT 'submitted' COMMENT '售后案件处理状态，默认submitted已提交', merchant_response VARCHAR(2000) COMMENT '商家对售后申请的回复', operator_note VARCHAR(1000) COMMENT '运营处理备注',
 refund_status VARCHAR(32) NOT NULL DEFAULT 'provider_unavailable' COMMENT '退款执行状态，默认provider_unavailable渠道不可用', refund_amount DECIMAL(14,2) COMMENT '售后退款金额，单位人民币元，保留两位小数',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '售后案件创建时间', updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '售后案件更新时间',
 INDEX idx_after_sales_order(order_id,created_at), INDEX idx_after_sales_status(status,created_at)
) COMMENT='订单售后申请、证据及退款处理记录';
CREATE TABLE IF NOT EXISTS t_consent_records (
 id CHAR(36) PRIMARY KEY COMMENT '同意留痕记录UUID', user_id CHAR(36) NOT NULL COMMENT '作出同意选择的用户ID', consent_type VARCHAR(40) NOT NULL COMMENT '用户同意事项类型',
 notice_version VARCHAR(80) NOT NULL COMMENT '所展示告知文本版本', recipient_name VARCHAR(200) COMMENT '信息接收方名称', recipient_contact VARCHAR(300) COMMENT '信息接收方联系方式',
 purpose VARCHAR(500) COMMENT '个人信息处理目的', information_types VARCHAR(1000) COMMENT '涉及的个人信息种类说明', rights_method VARCHAR(1000) COMMENT '用户行使权利的方式说明',
 accepted BOOLEAN NOT NULL COMMENT '用户是否同意：1同意、0不同意', accepted_at DATETIME NOT NULL COMMENT '用户作出同意选择的时间', source VARCHAR(40) NOT NULL COMMENT '同意选择的采集来源',
 INDEX idx_consent_user_type(user_id,consent_type,accepted_at)
) COMMENT='个人信息处理告知及用户同意选择留痕';
CREATE TABLE IF NOT EXISTS t_auth_credentials (
 user_id CHAR(36) NOT NULL PRIMARY KEY COMMENT '用户ID，主键', password_hash VARCHAR(120) NOT NULL COMMENT '密码单向散列值，不保存明文密码',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '密码凭据最近更新时间'
) COMMENT='用户密码认证凭据摘要';
CREATE TABLE IF NOT EXISTS t_login_codes (
 phone VARCHAR(32) PRIMARY KEY COMMENT '接收登录验证码的手机号，主键', code_hash CHAR(64) NOT NULL COMMENT '验证码摘要，不保存验证码明文', expires_at DATETIME NOT NULL COMMENT '登录验证码过期时间',
 attempts INT NOT NULL DEFAULT 0 COMMENT '验证码校验尝试次数，默认0', updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '验证码记录更新时间'
) COMMENT='手机号登录验证码摘要、有效期及校验次数';
CREATE TABLE IF NOT EXISTS t_auth_rate_limits (
 scope_name VARCHAR(32) NOT NULL COMMENT '认证限流场景名称，与主体摘要组成主键', subject_hash CHAR(64) NOT NULL COMMENT '限流主体摘要，与场景名称组成主键；不保存主体原文', used INT NOT NULL DEFAULT 0 COMMENT '当前限流窗口内已使用次数', window_end DATETIME NOT NULL COMMENT '当前限流窗口结束时间',
 PRIMARY KEY(scope_name,subject_hash)
) COMMENT='认证操作按主体及时间窗口限流计数';
CREATE TABLE IF NOT EXISTS t_sms_send_events (
 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '短信发送事件自增主键', phone_hash CHAR(64) NOT NULL COMMENT '接收手机号摘要，不保存完整明文手机号', ip_hash CHAR(64) NOT NULL COMMENT '请求来源IP摘要，不保存明文IP',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '短信发送事件记录时间', INDEX idx_sms_event_created(created_at)
) COMMENT='短信发送事件的手机号和来源IP摘要记录';
CREATE TABLE IF NOT EXISTS t_sms_daily_budget (budget_day DATE PRIMARY KEY COMMENT '短信发送统计日期，主键', send_count INT NOT NULL DEFAULT 0 COMMENT '当日累计发送次数，默认0') COMMENT='短信每日发送量计数';
CREATE TABLE IF NOT EXISTS t_store_applications (
 id CHAR(36) PRIMARY KEY COMMENT '开店申请UUID', user_id CHAR(36) NOT NULL COMMENT '提交开店申请的用户ID', business_name VARCHAR(200) NOT NULL COMMENT '经营主体名称',
 unified_social_credit_code VARCHAR(32) NOT NULL COMMENT '经营主体统一社会信用代码', legal_representative VARCHAR(100) NOT NULL COMMENT '经营主体法定代表人姓名',
 license_upload_id CHAR(36) NOT NULL COMMENT '营业执照上传记录ID', invitation_code VARCHAR(16) COMMENT '申请所使用的邀请码', application_channel VARCHAR(20) NOT NULL COMMENT '开店申请来源渠道',
 status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '开店申请审核状态，默认pending待审核', review_note VARCHAR(1000) COMMENT '开店审核备注', reviewed_by CHAR(36) COMMENT '审核运营用户ID', reviewed_at DATETIME COMMENT '开店审核时间',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开店申请创建时间', updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '开店申请更新时间',
 INDEX idx_store_app_status(status,created_at), INDEX idx_store_app_user(user_id,status)
) COMMENT='商家开店资质申请及运营审核记录';
CREATE TABLE IF NOT EXISTS t_stores (
 id CHAR(36) PRIMARY KEY COMMENT '店铺主键', owner_id CHAR(36) NOT NULL UNIQUE COMMENT '店主用户ID', application_id CHAR(36) NOT NULL UNIQUE COMMENT '获批的开店申请ID',
 name VARCHAR(200) NOT NULL COMMENT '店铺名称', description VARCHAR(1000) COMMENT '店铺简介', matching_fee_rate DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '店铺撮合费率参考配置（百分数）；当前代码固定5%，不额外重复收费',
 status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '店铺状态代码，默认active', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '店铺创建时间',
 CONSTRAINT chk_store_fee CHECK(matching_fee_rate >= 0 AND matching_fee_rate <= 10), INDEX idx_store_status(status)
) COMMENT='店铺主档及撮合费率配置';
CREATE TABLE IF NOT EXISTS t_store_insurance_policies (
 id CHAR(36) PRIMARY KEY COMMENT '保单记录主键', store_id CHAR(36) NOT NULL COMMENT '所属店铺ID', insurer_name VARCHAR(200) NOT NULL COMMENT '保险公司名称', policy_no VARCHAR(120) NOT NULL COMMENT '保险保单号',
 coverage_amount DECIMAL(14,2) NOT NULL COMMENT '保额，单位为currency指定币种的主货币单位', currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '保单币种代码，默认CNY', expires_at DATE NOT NULL COMMENT '保单到期日期',
 certificate_upload_id CHAR(36) NOT NULL COMMENT '保单凭证上传记录ID', status VARCHAR(40) NOT NULL DEFAULT 'pending_provider_verification' COMMENT '保单核验状态代码，默认pending_provider_verification',
 external_verification_ref VARCHAR(160) COMMENT '保险机构外部核验流水号', verification_channel VARCHAR(30) COMMENT '保单核验渠道代码', verified_by CHAR(36) COMMENT '核验操作人用户ID', verified_at DATETIME COMMENT '核验完成时间',
 review_note VARCHAR(1000) COMMENT '核验审核意见', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保单记录创建时间',
 INDEX idx_insurance_store_status(store_id,status,expires_at), UNIQUE KEY uq_insurer_policy(insurer_name,policy_no)
) COMMENT='店铺保险保单及外部核验记录';
CREATE TABLE IF NOT EXISTS t_insurance_coverages (
 id CHAR(36) PRIMARY KEY COMMENT '承保记录主键', order_id CHAR(36) NOT NULL UNIQUE COMMENT '被承保订单ID，每个订单唯一', policy_id CHAR(36) NOT NULL COMMENT '关联店铺保险保单ID',
 external_coverage_ref VARCHAR(160) NOT NULL COMMENT '保险机构承保流水号', declared_value DECIMAL(14,2) NOT NULL COMMENT '货物申报价值，单位为关联保单币种的主货币单位', premium_amount DECIMAL(14,2) NOT NULL COMMENT '保险机构直接收取的保费金额（关联保单币种主货币单位），不进入平台托管',
 premium_paid_directly BOOLEAN NOT NULL COMMENT '是否直接支付保险费，1是、0否', status VARCHAR(24) NOT NULL COMMENT '承保状态代码', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '承保记录创建时间',
 INDEX idx_insurance_coverage_status(status,created_at)
) COMMENT='订单关联的店铺保单承保记录';
CREATE TABLE IF NOT EXISTS t_risk_pool (id TINYINT PRIMARY KEY COMMENT '资金池记录主键', balance DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '历史资金池余额，保留两位小数；本表无独立币种，不作为平台承保或赔付承诺') COMMENT='历史风险准备金余额记录；不代表平台承保或担保';

-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 旧 products/orders 表已在 v3 去重时移除；原三条未执行的动态外键定义已删除。
-- 店铺/保单与订单、商品的关联需由后端接口校验；本次未擅自增加数据库字段。

-- ===== [3/6] integrations.sql：第三方厂商、产品与字段 (种子数据忽略重复键) =====

-- Cloud vendor catalog lives in these tables. Admins can add, remove, or replace products.
-- Runtime keys stay in t_integration_settings / t_integration_secrets.

CREATE TABLE IF NOT EXISTS t_integration_vendors (
  code VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '服务商代码，作为主键',
  name VARCHAR(80) NOT NULL COMMENT '服务商显示名称',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '目录排序值',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '服务商记录创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方集成服务商目录';

CREATE TABLE IF NOT EXISTS t_integration_products (
  vendor VARCHAR(32) NOT NULL COMMENT '所属服务商代码',
  code VARCHAR(64) NOT NULL COMMENT '集成产品代码，与服务商共同标识产品',
  name VARCHAR(80) NOT NULL COMMENT '集成产品显示名称',
  category VARCHAR(32) NOT NULL COMMENT '产品所属功能分类代码',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '产品目录排序值',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '产品记录创建时间',
  PRIMARY KEY (vendor, code),
  CONSTRAINT fk_integration_product_vendor FOREIGN KEY (vendor) REFERENCES t_integration_vendors(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方集成产品目录';

CREATE TABLE IF NOT EXISTS t_integration_product_fields (
  vendor VARCHAR(32) NOT NULL COMMENT '所属服务商代码',
  product VARCHAR(64) NOT NULL COMMENT '所属集成产品代码',
  field_key VARCHAR(64) NOT NULL COMMENT '配置字段键名',
  label VARCHAR(80) NOT NULL COMMENT '配置字段显示标签',
  secret TINYINT NOT NULL DEFAULT 0 COMMENT '是否为敏感字段，1是、0否；敏感明文不向管理目录返回',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '配置字段显示排序值',
  PRIMARY KEY (vendor, product, field_key),
  CONSTRAINT fk_integration_field_product FOREIGN KEY (vendor, product) REFERENCES t_integration_products(vendor, code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集成产品配置字段定义';

CREATE TABLE IF NOT EXISTS t_integration_settings (
  vendor VARCHAR(32) NOT NULL COMMENT '所属服务商代码',
  product VARCHAR(64) NOT NULL COMMENT '所属集成产品代码',
  enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用该集成产品，1是、0否',
  remark VARCHAR(200) NULL COMMENT '集成配置管理备注',
  updated_by VARCHAR(36) NULL COMMENT '最后修改人用户ID',
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '配置最后更新时间',
  PRIMARY KEY (vendor, product)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集成产品启用状态及管理备注';

CREATE TABLE IF NOT EXISTS t_integration_secrets (
  vendor VARCHAR(32) NOT NULL COMMENT '所属服务商代码',
  product VARCHAR(64) NOT NULL COMMENT '所属集成产品代码',
  field_key VARCHAR(64) NOT NULL COMMENT '配置字段键名',
  field_cipher TEXT NOT NULL COMMENT '配置值密文；按服务商、产品和字段作用域加密，仅运行配置解析时解密，禁止作为明文展示',
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '配置值最后更新时间',
  PRIMARY KEY (vendor, product, field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集成产品配置值密文存储';

-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- ===== [4/6] community_p0.sql：帖子、点赞与评论 =====

CREATE TABLE IF NOT EXISTS t_posts (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '帖子主键',
  author_id VARCHAR(36) NOT NULL COMMENT '发帖人用户ID',
  kind VARCHAR(16) NOT NULL COMMENT '帖子类型代码',
  title VARCHAR(200) NULL COMMENT '帖子标题',
  body VARCHAR(4000) NULL COMMENT '帖子正文',
  image_urls JSON NULL COMMENT '帖子图片URL列表，JSON数组',
  order_ref VARCHAR(36) NULL COMMENT '帖子关联订单ID',
  like_count INT NOT NULL DEFAULT 0 COMMENT '帖子点赞数量',
  comment_count INT NOT NULL DEFAULT 0 COMMENT '帖子评论数量',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '帖子审核或展示状态代码，默认pending',
  removed_reason VARCHAR(200) NULL COMMENT '帖子移除原因',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '帖子发布时间',
  INDEX idx_posts_status (status, created_at),
  INDEX idx_posts_author (author_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区帖子内容及互动计数';

CREATE TABLE IF NOT EXISTS t_post_likes (
  post_id VARCHAR(36) NOT NULL COMMENT '被点赞帖子ID',
  user_id VARCHAR(36) NOT NULL COMMENT '点赞用户ID',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '点赞时间',
  PRIMARY KEY (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区帖子点赞关系';

CREATE TABLE IF NOT EXISTS t_post_comments (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '评论主键',
  post_id VARCHAR(36) NOT NULL COMMENT '所属帖子ID',
  author_id VARCHAR(36) NOT NULL COMMENT '评论作者用户ID',
  content VARCHAR(500) NOT NULL COMMENT '评论文本内容',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '评论发布时间',
  INDEX idx_post_comments (post_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区帖子评论内容';

-- ===== [5/6] community_p1.sql：附件、媒体、商品锚点 =====

CREATE TABLE IF NOT EXISTS t_community_uploads (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '上传记录主键',
  user_id VARCHAR(36) NOT NULL COMMENT '上传者用户ID',
  kind VARCHAR(16) NOT NULL COMMENT '上传媒体类型代码',
  storage_key VARCHAR(400) NOT NULL COMMENT '文件在存储服务中的对象键',
  content_type VARCHAR(80) NULL COMMENT '文件媒体类型（MIME）',
  purpose VARCHAR(40) NULL COMMENT '上传用途代码',
  order_id CHAR(36) NULL COMMENT '关联订单ID',
  trip_id CHAR(36) NULL COMMENT '关联行程ID',
  sha256 CHAR(64) NULL COMMENT '文件内容SHA-256摘要，十六进制字符串',
  byte_size BIGINT NULL COMMENT '文件大小，单位字节',
  original_name VARCHAR(200) NULL COMMENT '上传文件原始名称',
  consumed_at TIMESTAMP(3) NULL COMMENT '上传文件被业务消费的时间',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传记录创建时间',
  INDEX idx_community_uploads_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区上传文件元数据及使用关联';

CREATE TABLE IF NOT EXISTS t_post_media (
  post_id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '帖子ID，每个帖子对应一条媒体记录',
  media_kind VARCHAR(16) NOT NULL COMMENT '媒体类型代码',
  media_url VARCHAR(800) NULL COMMENT '媒体访问URL',
  poster_url VARCHAR(800) NULL COMMENT '媒体封面访问URL',
  upload_id VARCHAR(36) NULL COMMENT '媒体文件上传记录ID',
  poster_upload_id VARCHAR(36) NULL COMMENT '封面文件上传记录ID',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '媒体关联创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子的媒体及封面关联';

CREATE TABLE IF NOT EXISTS t_post_anchors (
  post_id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '帖子ID，每个帖子对应一条挂载记录',
  anchor_type VARCHAR(16) NOT NULL COMMENT '挂载资源类型代码',
  anchor_id VARCHAR(36) NULL COMMENT '挂载资源ID',
  title_snapshot VARCHAR(200) NULL COMMENT '挂载时的资源标题快照',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '挂载记录创建时间',
  INDEX idx_post_anchors_target (anchor_type, anchor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子挂载的关联资源及标题快照';

-- ===== [6/6] community_p2.sql：收藏、分享、地点、会话与采购地初始化 =====

CREATE TABLE IF NOT EXISTS t_post_favorites (
  post_id VARCHAR(36) NOT NULL COMMENT '被收藏帖子ID',
  user_id VARCHAR(36) NOT NULL COMMENT '收藏用户ID',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '收藏时间',
  PRIMARY KEY (post_id, user_id),
  INDEX idx_post_favorites_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏社区帖子的关系';

CREATE TABLE IF NOT EXISTS t_post_shares (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '分享记录主键',
  post_id VARCHAR(36) NOT NULL COMMENT '被分享帖子ID',
  user_id VARCHAR(36) NOT NULL COMMENT '分享用户ID',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '分享发生时间',
  INDEX idx_post_shares_post (post_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子分享行为记录';

CREATE TABLE IF NOT EXISTS t_geo_points (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '坐标记录主键',
  ref_type VARCHAR(16) NOT NULL COMMENT '关联资源类型代码',
  ref_id VARCHAR(36) NOT NULL COMMENT '关联资源ID',
  kind VARCHAR(24) NOT NULL COMMENT '坐标点用途类型代码',
  lat DECIMAL(10,7) NOT NULL COMMENT '纬度，单位度',
  lng DECIMAL(10,7) NOT NULL COMMENT '经度，单位度',
  place_name VARCHAR(120) NULL COMMENT '地点名称',
  address VARCHAR(240) NULL COMMENT '地点详细地址',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '坐标记录创建时间',
  UNIQUE KEY uk_geo_ref (ref_type, ref_id, kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务资源关联的地理坐标点';

CREATE TABLE IF NOT EXISTS t_procurement_venues (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '采购场所主键',
  name VARCHAR(120) NOT NULL COMMENT '采购场所名称',
  city VARCHAR(64) NOT NULL COMMENT '采购场所所在城市',
  lat DECIMAL(10,7) NOT NULL COMMENT '场所纬度，单位度',
  lng DECIMAL(10,7) NOT NULL COMMENT '场所经度，单位度',
  address VARCHAR(240) NULL COMMENT '场所详细地址',
  partner_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否为合作场所，1是、0否',
  keywords VARCHAR(240) NULL COMMENT '场所检索关键词文本',
  active TINYINT NOT NULL DEFAULT 1 COMMENT '场所是否启用，1是、0否',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '场所记录创建时间',
  INDEX idx_venues_city (city, active, partner_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购场所目录及地理信息';

CREATE TABLE IF NOT EXISTS t_direct_threads (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '私信会话主键',
  user_low VARCHAR(36) NOT NULL COMMENT '会话双方中排序较小的用户ID',
  user_high VARCHAR(36) NOT NULL COMMENT '会话双方中排序较大的用户ID',
  post_id VARCHAR(36) NULL COMMENT '引发会话的来源帖子ID',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '会话创建时间',
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '会话最后更新时间',
  UNIQUE KEY uk_direct_pair (user_low, user_high)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户间私信会话及来源帖子';

CREATE TABLE IF NOT EXISTS t_direct_messages (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '私信消息主键',
  thread_id VARCHAR(36) NOT NULL COMMENT '所属私信会话ID',
  sender_id VARCHAR(36) NOT NULL COMMENT '消息发送人用户ID',
  content VARCHAR(500) NOT NULL COMMENT '私信消息文本',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息发送时间',
  INDEX idx_direct_messages_thread (thread_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信会话消息内容';

CREATE TABLE IF NOT EXISTS t_piggyback_requests (
  id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '捎带请求主键',
  trip_id VARCHAR(36) NOT NULL COMMENT '请求关联行程ID',
  post_id VARCHAR(36) NULL COMMENT '请求来源帖子ID',
  asker_id VARCHAR(36) NOT NULL COMMENT '提出捎带请求的用户ID',
  demand_id VARCHAR(36) NULL COMMENT '请求关联需求ID',
  title VARCHAR(200) NOT NULL COMMENT '捎带请求标题',
  note VARCHAR(4000) NULL COMMENT '捎带请求补充说明',
  city VARCHAR(64) NULL COMMENT '请求涉及城市',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '捎带请求处理状态代码，默认pending',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '捎带请求创建时间',
  INDEX idx_piggyback_trip (trip_id, status),
  INDEX idx_piggyback_asker (asker_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='针对行程发起的捎带请求';

-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。


-- ===== 收尾：suixinggou.sql 原触发器 (重建已存在的同名触发器) =====

DROP TRIGGER IF EXISTS trg_t_addresses_default_ins;
DROP TRIGGER IF EXISTS trg_t_addresses_default_upd;
DROP TRIGGER IF EXISTS trg_t_support_threads_uniq;
DROP TRIGGER IF EXISTS trg_t_privacy_bindings_active;
DROP TRIGGER IF EXISTS trg_t_reward_ledger_once;

DELIMITER //

-- 每用户最多一个默认地址
CREATE TRIGGER trg_t_addresses_default_ins
BEFORE INSERT ON t_addresses
FOR EACH ROW
BEGIN
  IF NEW.is_default = 1 THEN
    UPDATE t_addresses SET is_default = 0 WHERE user_id = NEW.user_id AND is_default = 1;
  END IF;
END//

CREATE TRIGGER trg_t_addresses_default_upd
BEFORE UPDATE ON t_addresses
FOR EACH ROW
BEGIN
  IF NEW.is_default = 1 AND (OLD.is_default = 0 OR OLD.is_default IS NULL) THEN
    UPDATE t_addresses SET is_default = 0 WHERE user_id = NEW.user_id AND is_default = 1 AND id <> NEW.id;
  END IF;
END//

-- AI 会话每用户唯一；peer 会话同用户+对端+订单唯一
CREATE TRIGGER trg_t_support_threads_uniq
BEFORE INSERT ON t_support_threads
FOR EACH ROW
BEGIN
  IF NEW.kind = 'ai' THEN
    IF EXISTS (SELECT 1 FROM t_support_threads WHERE kind = 'ai' AND user_id = NEW.user_id) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Duplicate AI thread for user';
    END IF;
  END IF;
  IF NEW.kind = 'peer' THEN
    IF EXISTS (SELECT 1 FROM t_support_threads WHERE kind = 'peer' AND user_id = NEW.user_id AND IFNULL(peer_id,'') = IFNULL(NEW.peer_id,'') AND IFNULL(order_id,'') = IFNULL(NEW.order_id,'')) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Duplicate peer thread';
    END IF;
  END IF;
END//

-- 每订单最多一个活跃虚拟号绑定
CREATE TRIGGER trg_t_privacy_bindings_active
BEFORE INSERT ON t_privacy_bindings
FOR EACH ROW
BEGIN
  IF NEW.status = 'active' THEN
    IF EXISTS (SELECT 1 FROM t_privacy_bindings WHERE order_id = NEW.order_id AND status = 'active') THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Duplicate active privacy binding for order';
    END IF;
  END IF;
END//

-- 分销奖励同一订单+邀请人+事件+层级只记一次（仅 order_id 非空时）
CREATE TRIGGER trg_t_reward_ledger_once
BEFORE INSERT ON t_reward_ledger
FOR EACH ROW
BEGIN
  IF NEW.order_id IS NOT NULL THEN
    IF EXISTS (SELECT 1 FROM t_reward_ledger WHERE order_id = NEW.order_id AND inviter_id = NEW.inviter_id AND event_type = NEW.event_type AND level = NEW.level) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Duplicate reward ledger entry';
    END IF;
  END IF;
END//

DELIMITER ;

-- ===== 恢复连接原有外键检查状态 =====
SET FOREIGN_KEY_CHECKS = @merged_previous_fk_checks;

-- 以下为同库增量结构，不重复导入原始表。
-- KuaixieTech: deployment-only incremental schema bundle.
-- Base: user-supplied 82-table schema; select the intended database before execution.
-- Generated by server/tools/build_deployment_sql.py. Do not edit this generated file.
-- Does not import reference/current-schema.sql, seed credentials, or modify business balances.
-- MySQL 8 DDL implicitly commits. Verify existing objects and a recoverable backup first.
-- CREATE TABLE IF NOT EXISTS does not validate an existing table definition.
-- No migration in this bundle has been executed by the coding agent.

-- BEGIN 001-missing-business-tables.proposed.sql
-- SHA256 11fe3776a604adeed52ef5740aff37d497cc78026624896355d964f7cc7ad6c6
-- PROPOSAL ONLY: review against live information_schema and a verified backup before execution.
-- MySQL 8.0+. Select the existing business database in the client; no USE or database creation here.
-- No application startup runner executes this file. No existing table or row is changed.
-- CREATE IF NOT EXISTS does not validate an existing table: run the metadata preflight first.

CREATE TABLE IF NOT EXISTS t_withdrawals (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT '提现申请主键',
  user_id CHAR(36) NOT NULL COMMENT '申请提现的用户ID',
  amount DECIMAL(12,2) NOT NULL COMMENT '申请提现金额，单位元，保留两位小数',
  channel VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT '提现渠道代码，默认manual',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '提现申请处理状态代码，默认pending',
  admin_note VARCHAR(2000) NULL COMMENT '提现审核管理备注',
  reviewed_by CHAR(36) NULL COMMENT '审核人用户ID',
  reviewed_at DATETIME(3) NULL COMMENT '提现审核时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '提现申请创建时间',
  INDEX idx_withdrawals_user_status (user_id,status),
  INDEX idx_withdrawals_queue (status,created_at),
  CONSTRAINT chk_withdrawals_amount CHECK(amount > 0),
  CONSTRAINT chk_withdrawals_status CHECK(status IN ('pending','approved','rejected','paid')),
  CONSTRAINT fk_withdrawals_user FOREIGN KEY(user_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户提现申请及后台审核记录';

CREATE TABLE IF NOT EXISTS t_announcements (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT '公告主键',
  title VARCHAR(255) NOT NULL COMMENT '公告默认语言标题',
  body TEXT NOT NULL COMMENT '公告默认语言正文',
  kind VARCHAR(32) NOT NULL DEFAULT 'system' COMMENT '公告类型代码，默认system',
  pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶，1是、0否',
  active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '公告是否启用，1是、0否',
  created_by CHAR(36) NOT NULL COMMENT '公告创建人用户ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '公告创建时间',
  title_i18n JSON NULL COMMENT '多语言公告标题，JSON对象，按语言代码存储',
  body_i18n JSON NULL COMMENT '多语言公告正文，JSON对象，按语言代码存储',
  INDEX idx_announcements_public (active,pinned,created_at),
  CONSTRAINT fk_announcements_creator FOREIGN KEY(created_by) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台公告及多语言内容';

CREATE TABLE IF NOT EXISTS t_proof_recipes (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT '凭证规则主键',
  trip_type VARCHAR(32) NOT NULL COMMENT '适用行程类型代码',
  transport_mode VARCHAR(32) NOT NULL COMMENT '适用交通方式代码',
  country_code VARCHAR(8) NULL COMMENT '适用国家或地区代码，空值表示未指定',
  items JSON NOT NULL COMMENT '规则要求的凭证材料项目，JSON结构',
  note VARCHAR(2000) NULL COMMENT '凭证规则补充说明',
  active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '规则是否启用，1是、0否',
  sort_order INT NOT NULL DEFAULT 100 COMMENT '规则排序值',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '凭证规则创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '凭证规则最后更新时间',
  INDEX idx_proof_recipes_scope (trip_type,transport_mode,country_code,active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按行程及交通方式配置的凭证材料规则';

-- Compatibility settings for existing RPCs. These are not proof of provider connectivity.
-- Existing t_integration_* tables remain the vendor/product/field catalog.
-- Do not duplicate or move existing secret values into config. A unified runtime adapter is pending.
CREATE TABLE IF NOT EXISTS t_channel_configs (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT '渠道配置主键',
  channel_type VARCHAR(32) NOT NULL COMMENT '渠道类型：sms短信、email邮件、wx_subscribe微信订阅、privacy_number隐私号、logistics物流、payment支付、ai人工智能、rtc实时通信',
  provider VARCHAR(64) NOT NULL COMMENT '渠道供应商标识',
  config JSON NOT NULL COMMENT '渠道参数JSON对象；可能包含凭据，管理端按敏感键名脱敏，不代表已加密',
  enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用该供应商配置，1启用、0停用',
  remark VARCHAR(2000) NULL COMMENT '配置说明',
  updated_by CHAR(36) NULL COMMENT '最后更新配置的管理员用户ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  UNIQUE KEY uk_channel_provider (channel_type,provider),
  INDEX idx_channel_enabled (channel_type,enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部渠道供应商配置，保存渠道参数与启用状态';

CREATE TABLE IF NOT EXISTS t_channel_secrets_probe (
  channel VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '渠道标识：wechat_subscribe、sms、email或ai_call',
  ready TINYINT(1) NOT NULL DEFAULT 0 COMMENT '渠道密钥是否标记就绪，1就绪、0未就绪；不代表真实调用成功',
  probed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最近记录探测状态的时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道密钥就绪状态的人工探测记录';

CREATE TABLE IF NOT EXISTS t_error_logs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增异常日志主键',
  scope VARCHAR(128) NOT NULL COMMENT '异常来源或作用域',
  message TEXT NOT NULL COMMENT '异常消息正文',
  stack MEDIUMTEXT NULL COMMENT '异常堆栈文本，可能含运行上下文，避免泄露敏感数据',
  page VARCHAR(1024) NULL COMMENT '发生异常的页面路径或地址',
  user_id CHAR(36) NULL COMMENT '关联用户ID',
  extra JSON NULL COMMENT '异常补充上下文JSON，避免记录凭据及敏感原文',
  status VARCHAR(16) NOT NULL DEFAULT 'open' COMMENT '处理状态：open待处理、resolved已处理',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '异常记录时间',
  resolved_at DATETIME(3) NULL COMMENT '标记处理完成的时间',
  INDEX idx_error_logs_queue (status,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端及服务异常日志与处理记录';

CREATE TABLE IF NOT EXISTS t_assist_requests (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT '协带申请主键',
  order_id CHAR(36) NOT NULL COMMENT '关联订单ID',
  requester_id CHAR(36) NOT NULL COMMENT '发起协带的原带单人用户ID',
  assistant_id CHAR(36) NULL COMMENT '承接协带的用户ID',
  share_rate DECIMAL(5,2) NOT NULL COMMENT '协带分润百分比，按货款与运费之和计算，代码允许20至60',
  note VARCHAR(2000) NULL COMMENT '协带需求说明',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '协带状态：pending待承接、accepted已承接、carrying携带中、done已完成、cancelled已取消',
  handover_a_urls JSON NOT NULL COMMENT '原带单人向协带人交接的照片地址JSON数组',
  handover_a_city VARCHAR(64) NULL COMMENT '首次交接所在城市',
  handover_a_at DATETIME(3) NULL COMMENT '首次交接凭证提交时间',
  handover_b_urls JSON NOT NULL COMMENT '协带人向买家交付的照片地址JSON数组',
  handover_b_city VARCHAR(64) NULL COMMENT '最终交付所在城市',
  finished_at DATETIME(3) NULL COMMENT '协带完成并结算的时间',
  settled_amount DECIMAL(12,2) NULL COMMENT '实际结算分润金额，单位人民币元',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '协带申请创建时间',
  INDEX idx_assist_order (order_id,status),
  INDEX idx_assist_assistant (assistant_id,status),
  CONSTRAINT chk_assist_share CHECK(share_rate >= 0 AND share_rate <= 100),
  CONSTRAINT chk_assist_status CHECK(status IN ('pending','accepted','carrying','done','cancelled')),
  CONSTRAINT fk_assist_order FOREIGN KEY(order_id) REFERENCES t_orders(id),
  CONSTRAINT fk_assist_requester FOREIGN KEY(requester_id) REFERENCES t_profiles(id),
  CONSTRAINT fk_assist_assistant FOREIGN KEY(assistant_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单协带申请、两次交接凭证与分润结算';

CREATE TABLE IF NOT EXISTS t_enterprise_license_files (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT '营业执照附件主键',
  user_id CHAR(36) NOT NULL COMMENT '提交营业执照的用户ID',
  storage_url VARCHAR(1024) NOT NULL COMMENT '营业执照文件存储地址，属认证资料，访问应受控',
  position_no INT NOT NULL DEFAULT 0 COMMENT '附件显示顺序编号',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '附件记录创建时间',
  INDEX idx_enterprise_license_user (user_id,position_no),
  CONSTRAINT fk_enterprise_license_user FOREIGN KEY(user_id) REFERENCES t_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业认证营业执照附件及排列顺序';

CREATE TABLE IF NOT EXISTS t_locales (
  code VARCHAR(16) NOT NULL PRIMARY KEY COMMENT '语言或地区代码，作为主键',
  native_name VARCHAR(64) NOT NULL COMMENT '该语言的本地名称',
  label_zh VARCHAR(64) NOT NULL COMMENT '语言的中文显示名称',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '语言选项排序值',
  active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用该语言，1启用、0停用',
  fallback_code VARCHAR(16) NULL COMMENT '翻译缺失时使用的回退语言代码',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '语言配置创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可用界面语言、显示名称与回退语言配置';

CREATE TABLE IF NOT EXISTS t_logistics_tracking (
  order_id CHAR(36) NOT NULL PRIMARY KEY COMMENT '关联订单ID，每单一条物流缓存',
  provider VARCHAR(64) NULL COMMENT '物流服务商或承运商标识',
  tracking_no VARCHAR(64) NULL COMMENT '物流运单号',
  latest_state VARCHAR(64) NULL COMMENT '最近一次查询返回的物流状态文本，含义由服务商定义',
  events_json JSON NULL COMMENT '物流轨迹事件JSON，结构随查询适配器返回内容确定',
  refreshed_at DATETIME(3) NULL COMMENT '最近刷新物流信息的时间',
  CONSTRAINT fk_logistics_tracking_order FOREIGN KEY(order_id) REFERENCES t_orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单物流单号及最近查询的轨迹缓存';
-- END 001-missing-business-tables.proposed.sql

-- BEGIN 002-configurable-payments.proposed.sql
-- SHA256 9c4d720a69253a4e62f421c7776ab734078f61ad2c7ccd200810511ff219c027
-- PROPOSAL ONLY. Existing business database; MySQL 8+. No balances are migrated or modified.
-- Review live metadata before application. No application startup executes this file.
CREATE TABLE IF NOT EXISTS t_payment_merchants (
 id CHAR(36) PRIMARY KEY COMMENT '支付商户配置主键', name VARCHAR(80) NOT NULL COMMENT '商户显示名称', adapter VARCHAR(40) NOT NULL COMMENT '支付渠道适配器标识',
 method VARCHAR(24) NOT NULL COMMENT '支付方式标识', platform VARCHAR(24) NOT NULL COMMENT '适用客户端平台标识', currencies JSON NOT NULL COMMENT '支持支付币种代码的JSON集合',
 credential_prefix VARCHAR(40) NULL COMMENT '支付凭据配置引用前缀，不是密钥明文', spread_bps INT NOT NULL DEFAULT 0 COMMENT '汇率加点，单位基点，1基点为0.01%',
 quote_ttl_seconds INT NOT NULL DEFAULT 300 COMMENT '报价有效期上限，单位秒', enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否启用该商户，1启用、0停用',
 revision BIGINT NOT NULL DEFAULT 1 COMMENT '商户配置版本号，用于检测报价后配置变化', settings JSON NOT NULL COMMENT '支付适配器设置JSON，含通知地址、返回地址、网关及收银台域名等配置', updated_by CHAR(36) NOT NULL COMMENT '最后更新配置的管理员用户ID',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 CONSTRAINT chk_payment_spread CHECK(spread_bps BETWEEN 0 AND 2000),
 CONSTRAINT chk_payment_ttl CHECK(quote_ttl_seconds BETWEEN 30 AND 900)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多币种支付商户、支付能力与报价参数配置';
CREATE TABLE IF NOT EXISTS t_fx_sources (
 id CHAR(36) PRIMARY KEY COMMENT '汇率源配置主键', name VARCHAR(80) NOT NULL COMMENT '汇率源显示名称', kind VARCHAR(32) NOT NULL COMMENT '汇率源适配类型：open_er_api或rates_json_v1',
 endpoint VARCHAR(1000) NULL COMMENT '汇率数据接口地址', credential_prefix VARCHAR(40) NULL COMMENT '访问凭据配置引用前缀，不是密钥明文',
 max_age_seconds INT NOT NULL DEFAULT 86400 COMMENT '可接受汇率数据的最长年龄，单位秒', enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否启用汇率源，1启用、0停用',
 transaction_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许该汇率源用于实际交易报价，1允许、0禁止', updated_by CHAR(36) NOT NULL COMMENT '最后更新配置的管理员用户ID',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外汇汇率数据源及交易使用开关配置';
CREATE TABLE IF NOT EXISTS t_payment_quotes (
 id CHAR(36) PRIMARY KEY COMMENT '支付报价主键', user_id CHAR(36) NOT NULL COMMENT '申请报价的用户ID', merchant_id CHAR(36) NOT NULL COMMENT '支付商户配置ID',
 merchant_revision BIGINT NOT NULL COMMENT '报价时商户配置版本号', merchant_snapshot JSON NOT NULL COMMENT '报价时商户配置JSON快照，含配置引用，按敏感支付配置受控访问',
 base_currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '基准金额币种代码，默认CNY', base_minor DECIMAL(24,0) NOT NULL COMMENT '基准金额的最小货币单位整数，CNY时单位分',
 payment_currency CHAR(3) NOT NULL COMMENT '实际支付币种代码', payment_minor DECIMAL(24,0) NOT NULL COMMENT '支付金额的最小货币单位整数，小数位由payment_minor_digits定义', payment_minor_digits INT NOT NULL COMMENT '支付币种主单位的小数位数',
 fx_rate DECIMAL(36,18) NOT NULL COMMENT '从基准币种换算为支付币种的汇率', spread_bps INT NOT NULL COMMENT '本次报价汇率加点，单位基点，1基点为0.01%', rate_source VARCHAR(80) NOT NULL COMMENT '本次报价使用的汇率来源标识',
 rate_as_of TIMESTAMP(3) NOT NULL COMMENT '汇率数据对应时间', expires_at TIMESTAMP(3) NOT NULL COMMENT '报价失效时间',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '报价创建时间',
 INDEX idx_payment_quotes_user(user_id,created_at),
 CONSTRAINT chk_quote_cny CHECK(base_currency='CNY'),
 CONSTRAINT chk_quote_amount CHECK(base_minor>0 AND payment_minor>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锁定支付商户配置和汇率的限时支付报价';
CREATE TABLE IF NOT EXISTS t_currency_payments (
 id CHAR(32) PRIMARY KEY COMMENT '支付单主键，32位标识', user_id CHAR(36) NOT NULL COMMENT '充值用户ID', request_id CHAR(36) NOT NULL COMMENT '用户请求幂等标识',
 quote_id CHAR(36) NOT NULL COMMENT '锁定的支付报价ID', merchant_id CHAR(36) NOT NULL COMMENT '支付商户配置ID', merchant_snapshot JSON NOT NULL COMMENT '支付时商户配置JSON快照，用于后续查询核验，敏感配置受控访问',
 base_currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '基准充值币种代码，默认CNY', base_minor DECIMAL(24,0) NOT NULL COMMENT '基准充值金额的最小货币单位整数，CNY时单位分',
 payment_currency CHAR(3) NOT NULL COMMENT '支付币种代码', payment_minor DECIMAL(24,0) NOT NULL COMMENT '支付金额的最小货币单位整数，小数位由payment_minor_digits定义', payment_minor_digits INT NOT NULL COMMENT '支付币种主单位的小数位数',
 status VARCHAR(16) NOT NULL DEFAULT 'creating' COMMENT '支付状态：creating创建中、pending待支付、paid已核验到账', checkout_kind VARCHAR(24) NULL COMMENT '收银台类型：dispatching请求已占用，或redirect、qr_text、wechat_jsapi、wechat_app、alipay_app',
 checkout_value TEXT NULL COMMENT '收银台跳转地址、二维码文本或客户端支付参数，可能含临时支付数据，访问应受控', provider_transaction_id VARCHAR(128) NULL COMMENT '渠道交易流水号',
 last_checked_at TIMESTAMP(3) NULL COMMENT '最近核查时间', last_error_code VARCHAR(64) NULL COMMENT '最近一次支付查询的安全错误码',
 paid_at TIMESTAMP(3) NULL COMMENT '支付成功核验入账时间', created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 UNIQUE KEY uk_currency_payment_request(user_id,request_id), UNIQUE KEY uk_currency_payment_quote(quote_id),
 UNIQUE KEY uk_currency_payment_transaction(merchant_id,provider_transaction_id),
 INDEX idx_currency_payment_pending(status,created_at),
 CONSTRAINT chk_currency_payment_status CHECK(status IN('creating','pending','paid')),
 CONSTRAINT chk_currency_payment_cny CHECK(base_currency='CNY'),
 CONSTRAINT chk_currency_payment_amount CHECK(base_minor>0 AND payment_minor>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多币种充值支付尝试、渠道收银台信息与核验状态';
CREATE TABLE IF NOT EXISTS t_currency_payment_receipts (
 id CHAR(36) PRIMARY KEY COMMENT '回执主键', payment_id CHAR(32) NOT NULL COMMENT '关联支付单ID',
 merchant_id CHAR(36) NOT NULL COMMENT '支付商户配置ID', provider_transaction_id VARCHAR(128) NOT NULL COMMENT '已核验的渠道交易流水号',
 payment_currency CHAR(3) NOT NULL COMMENT '回执支付币种代码', payment_minor DECIMAL(24,0) NOT NULL COMMENT '已核验支付金额的最小货币单位整数', payment_minor_digits INT NOT NULL COMMENT '支付币种主单位的小数位数',
 base_minor DECIMAL(24,0) NOT NULL COMMENT '对应基准充值金额的最小货币单位整数，人民币以分计', verified_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '渠道回执核验并持久化时间',
 UNIQUE KEY uk_currency_receipt_payment(payment_id),
 UNIQUE KEY uk_currency_receipt_provider(merchant_id,provider_transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已核验支付回执，用于防止重复充值入账';
-- Intentionally no default enabled merchant, no sample exchange rates, no fake credentials.
-- END 002-configurable-payments.proposed.sql

-- BEGIN 003-goods-insurance.proposed.sql
-- SHA256 59b81fa70d669dd8268244cf1b02f07510488f5fbded05c9f0900fb1fcc553aa
-- PROPOSAL ONLY. Existing insurance/store policy tables and historical balances are not rewritten.
-- Consumer insurance is issued by a contracted insurer, paid directly to that insurer.
CREATE TABLE IF NOT EXISTS t_goods_insurance_products (
 id VARCHAR(64) PRIMARY KEY COMMENT '保险产品标识',
 insurer_code VARCHAR(64) NOT NULL COMMENT '承保保险机构代码',
 config_json JSON NOT NULL COMMENT '保险产品配置JSON，含适配器、产品代码、凭据引用前缀、条款及服务域名等；不应存放密钥明文',
 enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '产品是否启用，1启用、0停用',
 revision BIGINT NOT NULL DEFAULT 1 COMMENT '保险产品配置版本号',
 updated_by CHAR(36) NOT NULL COMMENT '最后更新产品的管理员用户ID',
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货物保险产品及保险方接入参数配置';
CREATE TABLE IF NOT EXISTS t_goods_insurance_quotes (
 id CHAR(36) PRIMARY KEY COMMENT '保险报价主键',
 user_id CHAR(36) NOT NULL COMMENT '申请报价的用户ID',
 subject_type VARCHAR(16) NOT NULL COMMENT '报价标的类型：trip行程、pk_quote竞价报价、order订单',
 platform VARCHAR(16) NOT NULL COMMENT '发起报价的客户端平台：web、h5、app或mp_weixin',
 subject_id CHAR(36) NOT NULL COMMENT '对应行程、竞价报价或订单ID',
 item_name VARCHAR(255) NOT NULL COMMENT '投保货物名称',
 product_id VARCHAR(64) NOT NULL COMMENT '保险产品标识',
 product_revision BIGINT NOT NULL COMMENT '报价时保险产品配置版本号',
 declared_value DECIMAL(14,2) NOT NULL COMMENT '货物申报价值，按输入货物价格主单位记录；当前订单货款为人民币元',
 premium DECIMAL(24,6) NOT NULL COMMENT '保险方报价保费，单位为currency对应币种的主单位',
 currency CHAR(3) NOT NULL COMMENT '保险保费币种代码',
 provider_ref VARCHAR(160) NOT NULL COMMENT '保险方返回的报价引用标识',
 terms_version VARCHAR(160) NOT NULL COMMENT '保险方返回的条款版本标识',
 snapshot_json JSON NOT NULL COMMENT '报价时保险产品配置JSON快照，含凭据引用，访问应受控',
 expires_at DATETIME(3) NOT NULL COMMENT '报价失效时间',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '报价创建时间',
 INDEX idx_goods_insurance_quote_user(user_id,created_at),
 UNIQUE KEY uq_goods_insurance_quote_ref(product_id,provider_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保险方返回的货物保险限时报价及产品快照';
CREATE TABLE IF NOT EXISTS t_goods_insurance_applications (
 id CHAR(36) PRIMARY KEY COMMENT '投保申请主键',
 user_id CHAR(36) NOT NULL COMMENT '投保用户ID',
 order_id CHAR(36) NOT NULL COMMENT '关联订单ID',
 quote_id CHAR(36) NOT NULL COMMENT '采用的保险报价ID',
 insurer_code VARCHAR(64) NOT NULL COMMENT '保险机构代码',
 request_id CHAR(36) NOT NULL COMMENT '用户投保请求幂等标识',
 status VARCHAR(32) NOT NULL DEFAULT 'reserved' COMMENT '投保状态：reserved已预留、dispatching提交中、awaiting_provider待保险方确认、scheduled待生效、active已生效、rejected拒保、cancelled已取消、expired已过期',
 provider_application_ref VARCHAR(160) NULL COMMENT '保险方申请引用标识',
 checkout_url VARCHAR(1500) NULL COMMENT '保险方收银台地址，可能包含临时访问参数',
 policy_number VARCHAR(160) NULL COMMENT '保险方核验返回的保单号',
 coverage_ref VARCHAR(160) NULL COMMENT '保险保障记录引用标识',
 certificate_url VARCHAR(1500) NULL COMMENT '保险凭证地址，含投保资料，访问应受控',
 coverage_starts_at DATETIME(3) NULL COMMENT '保障起始时间',
 coverage_ends_at DATETIME(3) NULL COMMENT '保障结束时间',
 terms_accepted_at DATETIME(3) NOT NULL COMMENT '用户确认保险条款的时间',
 disclosures_accepted_at DATETIME(3) NOT NULL COMMENT '用户确认告知事项的时间',
 last_checked_at DATETIME(3) NULL COMMENT '最近向保险方核查的时间',
 last_error_code VARCHAR(64) NULL COMMENT '最近核查的安全错误码',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '申请创建时间',
 UNIQUE KEY uq_goods_insurance_order(order_id),
 UNIQUE KEY uq_goods_insurance_quote(quote_id),
 UNIQUE KEY uq_goods_insurance_request(user_id,request_id),
 UNIQUE KEY uq_goods_insurance_coverage(insurer_code,coverage_ref),
 UNIQUE KEY uq_goods_insurance_provider_application(insurer_code,provider_application_ref),
 INDEX idx_goods_insurance_pending(status,last_checked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单保险投保申请及承保方核验结果';
-- END 003-goods-insurance.proposed.sql

-- BEGIN 004-dispute-events.proposed.sql
-- SHA256 342c712441621c3b8112e8c457dd80d1181356171d4f400c9743a379686402af
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
-- END 004-dispute-events.proposed.sql

-- BEGIN 005-store-products.proposed.sql
-- SHA256 9f058d5c93aabca7ac72cf83bc52029532bca17c94505a1d5c85d4c3142fe683
-- Deployment proposal only: explicit ownership-checked storefront associations.
-- No changes to historical orders, prices, commission rates, insurance or account balances.
CREATE TABLE IF NOT EXISTS t_store_products (
 store_id CHAR(36) NOT NULL COMMENT '店铺ID',
 product_id CHAR(36) NOT NULL COMMENT '商品ID',
 created_by CHAR(36) NOT NULL COMMENT '建立店铺商品关联的用户ID',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '关联创建时间',
 PRIMARY KEY(store_id,product_id),
 UNIQUE KEY uq_store_product(product_id),
 CONSTRAINT fk_store_product_store FOREIGN KEY(store_id) REFERENCES t_stores(id),
 CONSTRAINT fk_store_product_product FOREIGN KEY(product_id) REFERENCES t_products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺与商品的归属关联';
-- END 005-store-products.proposed.sql

-- BEGIN 006-commerce-checkout.proposed.sql
-- SHA256 40229acd3d8ba8cc219d67c5b5ac275cebcef9744978f296b2094ab5c5f3e1c9
-- Deployment-only proposal: product checkout attribution and exact stock reservation state.
-- Stock is decremented in the same transaction as order escrow; no historical backfill or fund writes here.
CREATE TABLE IF NOT EXISTS t_product_order_reservations (
 order_id CHAR(36) NOT NULL PRIMARY KEY COMMENT '关联订单ID，作为预留记录主键',
 product_id CHAR(36) NOT NULL COMMENT '下单商品ID',
 buyer_id CHAR(36) NOT NULL COMMENT '买家用户ID',
 vendor_id CHAR(36) NOT NULL COMMENT '商品卖家及带单人用户ID',
 trip_id CHAR(36) NOT NULL COMMENT '用于履约的行程ID',
 store_id CHAR(36) NULL COMMENT '下单时商品归属店铺ID',
 quantity INT NOT NULL COMMENT '下单商品数量',
 unit_price DECIMAL(12,2) NOT NULL COMMENT '商品成交单价，单位为currency对应币种的主单位',
 goods_amount DECIMAL(12,2) NOT NULL COMMENT '商品货款总额，单位为currency对应币种的主单位',
 service_fee DECIMAL(12,2) NOT NULL COMMENT '订单服务费，单位为currency对应币种的主单位',
 currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '金额币种代码，当前下单写入CNY',
 stock_state VARCHAR(16) NOT NULL DEFAULT 'held' COMMENT '库存占用状态：held已预留、consumed已消耗、released已释放',
 request_hash CHAR(64) NOT NULL COMMENT '下单请求内容哈希，用于核对幂等请求一致性',
 snapshot JSON NOT NULL COMMENT '下单时商品与订单约定JSON快照，用于核对计价和归属',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 INDEX idx_product_reservation_state(product_id,stock_state),
 INDEX idx_store_order_attribution(store_id,created_at),
 CONSTRAINT chk_product_order_quantity CHECK(quantity BETWEEN 1 AND 100),
 CONSTRAINT chk_product_order_stock_state CHECK(stock_state IN('held','consumed','released')),
 CONSTRAINT fk_product_order_order FOREIGN KEY(order_id) REFERENCES t_orders(id),
 CONSTRAINT fk_product_order_product FOREIGN KEY(product_id) REFERENCES t_products(id),
 CONSTRAINT fk_product_order_store FOREIGN KEY(store_id) REFERENCES t_stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品下单时的库存占用、计价与归属快照';
-- END 006-commerce-checkout.proposed.sql

-- BEGIN 007-refunds.proposed.sql
-- SHA256 7b3b9697427954d050684c58a58a790e046a5455880036071ea6c6858406a4f5
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
-- END 007-refunds.proposed.sql

-- BEGIN 008-coupler-environments.proposed.sql
-- SHA256 48d4aa5af570b7dd26e68ca4a11ffd969736475b628bccc4e2102f24e152c0fa
-- Deployment-only proposal: isolated API configuration environments.
-- No startup DDL, no copying legacy plaintext/ciphertext, no business balance or payment mutation.
CREATE TABLE IF NOT EXISTS t_integration_runtime_state (
 id TINYINT NOT NULL PRIMARY KEY COMMENT '运行状态单例主键，代码使用1',
 current_mode VARCHAR(8) NOT NULL DEFAULT 'SANDBOX' COMMENT '当前集成运行模式：SANDBOX沙箱或LIVE真实环境',
 revision BIGINT NOT NULL DEFAULT 0 COMMENT '运行模式版本号，用于并发变更校验',
 updated_by CHAR(36) NULL COMMENT '最后切换模式的管理员用户ID',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 CONSTRAINT chk_coupler_runtime_id CHECK(id=1),
 CONSTRAINT chk_coupler_runtime_mode CHECK(current_mode IN('SANDBOX','LIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部集成当前运行模式及切换版本';
CREATE TABLE IF NOT EXISTS t_integration_mode_scopes (
 mode VARCHAR(8) NOT NULL COMMENT '集成运行模式：SANDBOX沙箱或LIVE真实环境',
 category VARCHAR(32) NOT NULL COMMENT '集成能力类别代码',
 selected_provider VARCHAR(32) NULL COMMENT '当前为该类别选择的供应商代码',
 revision BIGINT NOT NULL DEFAULT 0 COMMENT '类别选择状态版本号，用于并发变更校验',
 updated_by CHAR(36) NULL COMMENT '最后更新选择的管理员用户ID',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 PRIMARY KEY(mode,category),
 CONSTRAINT chk_coupler_scope_mode CHECK(mode IN('SANDBOX','LIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='各运行模式下集成类别的供应商选择与版本';
CREATE TABLE IF NOT EXISTS t_integration_mode_configs (
 mode VARCHAR(8) NOT NULL COMMENT '集成运行模式：SANDBOX沙箱或LIVE真实环境',
 category VARCHAR(32) NOT NULL COMMENT '集成能力类别代码',
 provider VARCHAR(32) NOT NULL COMMENT '集成供应商代码',
 enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '供应商在该模式下是否启用，1启用、0停用',
 config_revision BIGINT NOT NULL DEFAULT 0 COMMENT '当前引用的供应商配置值版本号',
 updated_by CHAR(36) NULL COMMENT '最后更新配置的管理员用户ID',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 PRIMARY KEY(mode,category,provider),
 CONSTRAINT chk_coupler_config_mode CHECK(mode IN('SANDBOX','LIVE')),
 CONSTRAINT fk_coupler_config_scope FOREIGN KEY(mode,category) REFERENCES t_integration_mode_scopes(mode,category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按运行模式与集成类别隔离的供应商启用配置';
CREATE TABLE IF NOT EXISTS t_integration_config_values (
 mode VARCHAR(8) NOT NULL COMMENT '集成运行模式：SANDBOX沙箱或LIVE真实环境',
 category VARCHAR(32) NOT NULL COMMENT '集成能力类别代码',
 provider VARCHAR(32) NOT NULL COMMENT '集成供应商代码',
 config_revision BIGINT NOT NULL COMMENT '该配置值所属版本号',
 field_key VARCHAR(64) NOT NULL COMMENT '配置字段键名',
 field_cipher TEXT NOT NULL COMMENT 'AES-GCM加密的配置值，认证上下文绑定模式、类别、供应商、版本和字段；禁止作为明文展示',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '加密配置值创建时间',
 PRIMARY KEY(mode,category,provider,config_revision,field_key),
 CONSTRAINT chk_coupler_value_mode CHECK(mode IN('SANDBOX','LIVE')),
 CONSTRAINT chk_coupler_value_revision CHECK(config_revision>0),
 CONSTRAINT fk_coupler_value_config FOREIGN KEY(mode,category,provider) REFERENCES t_integration_mode_configs(mode,category,provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按模式、供应商和版本隔离的加密集成配置值';
-- END 008-coupler-environments.proposed.sql

-- BEGIN 009-order-escrow-refunds.proposed.sql
-- SHA256 d2881b3a5b9f832c4051b9f16c230d54e5e6412471eec7f6ebfcfacbb3120c13
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
-- END 009-order-escrow-refunds.proposed.sql

-- BEGIN 010-upload-bindings.proposed.sql
-- SHA256 a89cc015bfbd59b1b0e823facac95b855711a4d55859c16725d5afe806bbe5df
-- Deployment proposal only. Keep private objects private; no business rows are backfilled or deleted.
CREATE TABLE IF NOT EXISTS t_upload_bindings (
 upload_id VARCHAR(36) NOT NULL COMMENT '上传文件记录ID',
 domain_type VARCHAR(20) NOT NULL COMMENT '使用文件的领域类型标识',
 resource_id CHAR(36) NOT NULL COMMENT '使用文件的领域资源ID',
 created_by CHAR(36) NOT NULL COMMENT '建立文件绑定的用户ID',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '绑定创建时间',
 PRIMARY KEY(upload_id,domain_type,resource_id),
 INDEX idx_upload_binding_resource(domain_type,resource_id),
 CONSTRAINT chk_upload_binding_domain CHECK(domain_type IN('product','order','dispute'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传文件与使用该文件的领域资源之间的引用绑定';
CREATE TABLE IF NOT EXISTS t_upload_deletions (
 id CHAR(36) PRIMARY KEY COMMENT '文件删除请求主键，由上传文件标识确定',
 upload_id VARCHAR(36) NOT NULL COMMENT '待删除的上传文件记录ID',
 user_id CHAR(36) NOT NULL COMMENT '发起删除的文件所属用户ID',
 status VARCHAR(16) NOT NULL COMMENT '删除状态：deleting删除中或待存储确认、deleted已确认删除',
 last_error_code VARCHAR(64) COMMENT '对象存储删除确认失败的安全错误码',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '删除请求创建时间',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '删除状态最后更新时间',
 UNIQUE KEY uq_upload_deletion(upload_id),
 INDEX idx_upload_deletion_user(user_id,created_at),
 CONSTRAINT chk_upload_deletion_status CHECK(status IN('deleting','deleted'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传文件删除请求及对象存储删除确认日志';
-- END 010-upload-bindings.proposed.sql

-- BEGIN 011-order-status-width.proposed.sql
-- SHA256 a540494713ebcc2bbde0f61de85586eb2b534dbb6be4865574b5516c88beb4c3
-- Deployment-only widening for existing status cancelled_no_fault (18 characters).
-- Preserve the existing public state protocol. Do not shrink an already wider column.
-- Requires the intended 82-table business database to be selected before execution.
SET @kuaixie_order_status_ddl = IF(
 EXISTS(SELECT 1 FROM information_schema.columns
        WHERE table_schema=DATABASE() AND table_name='t_orders' AND column_name='status'
          AND data_type='varchar' AND character_maximum_length<32),
 'ALTER TABLE t_orders MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT ''requesting'' COMMENT ''订单状态机''',
 'SELECT ''Order status widening skipped; verify existing definition in deployment preflight'' AS migration_status'
);
PREPARE kuaixie_order_status_stmt FROM @kuaixie_order_status_ddl;
EXECUTE kuaixie_order_status_stmt;
DEALLOCATE PREPARE kuaixie_order_status_stmt;
SET @kuaixie_order_status_ddl = NULL;
-- END 011-order-status-width.proposed.sql

