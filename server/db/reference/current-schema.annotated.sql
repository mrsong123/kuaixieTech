-- 原始82表的中文备注副本；包含原始种子和触发器，不用于直接重复导入已有业务库。

CREATE DATABASE IF NOT EXISTS `suixinggou` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `suixinggou`;
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
INSERT IGNORE INTO t_risk_pool(id,balance) VALUES(1,0);
INSERT IGNORE INTO t_categories(id,parent_id,level,name,region,ref_price_min,ref_price_max) VALUES
(1,NULL,1,'日本','日本',NULL,NULL),(2,NULL,1,'韩国','韩国',NULL,NULL),(3,NULL,1,'法国','法国',NULL,NULL),(4,NULL,1,'中国','中国',NULL,NULL),
(10,1,2,'美妆护肤','日本',120,2800),(11,1,2,'箱包配饰','日本',680,12000),(12,1,2,'数码电子','日本',480,8900),
(20,2,2,'美妆护肤','韩国',80,1800),(21,2,2,'食品保健','韩国',60,900),(30,3,2,'特色好物','法国',200,20000);
-- 将原种子中的国家标签同时记录到原有国家关联表，不引入新表/新列。
INSERT IGNORE INTO t_category_countries(category_id,country_code) VALUES
(1,'JP'),(10,'JP'),(11,'JP'),(12,'JP'),
(2,'KR'),(20,'KR'),(21,'KR'),(3,'FR'),(30,'FR'),(4,'CN');

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

-- seed-once (bootstrap skips these INSERTs when t_integration_products already has rows)

INSERT IGNORE INTO t_integration_vendors(code,name,sort_no) VALUES
('aliyun','阿里云',10),
('tencent','腾讯云',20),
('wechat','微信',30),
('amap','高德',40),
('cursor','Cursor',50),
('kdniao','快递鸟',60),
('sf','顺丰',70),
('http-adapter','通用 HTTPS 适配器',80);

INSERT IGNORE INTO t_integration_products(vendor,code,name,category,sort_no) VALUES
('aliyun','sms','短信 SMS','sms',10),
('aliyun','oss','对象存储 OSS','oss',20),
('aliyun','privacy-number','号码隐私保护','rtc',30),
('aliyun','green','内容安全','ai',40),
('tencent','sms','短信 SMS','sms',10),
('tencent','cos','对象存储 COS','oss',20),
('wechat','miniprogram','小程序','identity',10),
('wechat','pay','微信支付 APIv3','pay',20),
('amap','maps','地图 / 导航','map',10),
('cursor','agent-api','Cloud Agents API','ai',10),
('kdniao','logistics','物流查询','logistics',10),
('sf','express','物流','logistics',10),
('http-adapter','invite-qr','邀请码海报','adapter',10),
('http-adapter','contact-code-login','验证码登录通道','adapter',20),
('http-adapter','rtc-call','实时通话','adapter',30),
('http-adapter','notify-push','消息推送','adapter',40),
('http-adapter','send-contact-code','短信投递适配器','adapter',50),
('http-adapter','wechat-pay','微信支付适配器','adapter',60),
('http-adapter','translate-msg','聊天翻译','adapter',70),
('http-adapter','logistics-query','物流查询适配器','adapter',80),
('http-adapter','privacy-call','隐私号','adapter',90),
('http-adapter','support-ai-reply','客服模型','adapter',100),
('http-adapter','channel-test','通道探测','adapter',110);

INSERT IGNORE INTO t_integration_product_fields(vendor,product,field_key,label,secret,sort_no) VALUES
('aliyun','sms','access_key_id','AccessKey ID',0,10),
('aliyun','sms','access_key_secret','AccessKey Secret',1,20),
('aliyun','sms','sign_name','短信签名',0,30),
('aliyun','sms','template_code','模板 CODE',0,40),
('aliyun','sms','region','地域',0,50),
('aliyun','oss','access_key_id','AccessKey ID',0,10),
('aliyun','oss','access_key_secret','AccessKey Secret',1,20),
('aliyun','oss','bucket','Bucket',0,30),
('aliyun','oss','endpoint','Endpoint',0,40),
('aliyun','oss','public_base_url','公网访问前缀',0,50),
('aliyun','privacy-number','access_key_id','AccessKey ID',0,10),
('aliyun','privacy-number','access_key_secret','AccessKey Secret',1,20),
('aliyun','privacy-number','pool_key','号码池 Key',1,30),
('aliyun','green','access_key_id','AccessKey ID',0,10),
('aliyun','green','access_key_secret','AccessKey Secret',1,20),
('aliyun','green','region','地域',0,30),
('tencent','sms','secret_id','SecretId',0,10),
('tencent','sms','secret_key','SecretKey',1,20),
('tencent','sms','sdk_app_id','SdkAppId',0,30),
('tencent','sms','sign_name','短信签名',0,40),
('tencent','sms','template_id','模板 ID',0,50),
('tencent','cos','secret_id','SecretId',0,10),
('tencent','cos','secret_key','SecretKey',1,20),
('tencent','cos','bucket','Bucket',0,30),
('tencent','cos','region','地域',0,40),
('tencent','cos','public_base_url','公网访问前缀',0,50),
('wechat','miniprogram','app_id','AppId',0,10),
('wechat','miniprogram','app_secret','AppSecret',1,20),
('wechat','pay','app_id','AppId',0,10),
('wechat','pay','mch_id','商户号',0,20),
('wechat','pay','api_v3_key','APIv3 密钥',1,30),
('wechat','pay','cert_serial','证书序列号',0,40),
('wechat','pay','private_key_pem','商户私钥 PEM',1,50),
('wechat','pay','notify_url','支付回调 URL',0,60),
('amap','maps','web_key','Web Key',1,10),
('amap','maps','rest_key','REST Key',1,20),
('cursor','agent-api','api_key','API Key',1,10),
('cursor','agent-api','api_base','API Base URL',0,20),
('kdniao','logistics','ebusiness_id','EBusinessID',0,10),
('kdniao','logistics','api_key','ApiKey',1,20),
('sf','express','partner_id','顾客编码',0,10),
('sf','express','check_word','校验码',1,20),
('sf','express','monthly_card','月结卡号',0,30),
('http-adapter','invite-qr','url','HTTPS 地址',0,10),
('http-adapter','invite-qr','bearer_token','Bearer Token',1,20),
('http-adapter','invite-qr','timeout_seconds','超时秒',0,30),
('http-adapter','contact-code-login','url','HTTPS 地址',0,10),
('http-adapter','contact-code-login','bearer_token','Bearer Token',1,20),
('http-adapter','contact-code-login','timeout_seconds','超时秒',0,30),
('http-adapter','rtc-call','url','HTTPS 地址',0,10),
('http-adapter','rtc-call','bearer_token','Bearer Token',1,20),
('http-adapter','rtc-call','timeout_seconds','超时秒',0,30),
('http-adapter','notify-push','url','HTTPS 地址',0,10),
('http-adapter','notify-push','bearer_token','Bearer Token',1,20),
('http-adapter','notify-push','timeout_seconds','超时秒',0,30),
('http-adapter','send-contact-code','url','HTTPS 地址',0,10),
('http-adapter','send-contact-code','bearer_token','Bearer Token',1,20),
('http-adapter','send-contact-code','timeout_seconds','超时秒',0,30),
('http-adapter','wechat-pay','url','HTTPS 地址',0,10),
('http-adapter','wechat-pay','bearer_token','Bearer Token',1,20),
('http-adapter','wechat-pay','timeout_seconds','超时秒',0,30),
('http-adapter','translate-msg','url','HTTPS 地址',0,10),
('http-adapter','translate-msg','bearer_token','Bearer Token',1,20),
('http-adapter','translate-msg','timeout_seconds','超时秒',0,30),
('http-adapter','logistics-query','url','HTTPS 地址',0,10),
('http-adapter','logistics-query','bearer_token','Bearer Token',1,20),
('http-adapter','logistics-query','timeout_seconds','超时秒',0,30),
('http-adapter','privacy-call','url','HTTPS 地址',0,10),
('http-adapter','privacy-call','bearer_token','Bearer Token',1,20),
('http-adapter','privacy-call','timeout_seconds','超时秒',0,30),
('http-adapter','support-ai-reply','url','HTTPS 地址',0,10),
('http-adapter','support-ai-reply','bearer_token','Bearer Token',1,20),
('http-adapter','support-ai-reply','timeout_seconds','超时秒',0,30),
('http-adapter','channel-test','url','HTTPS 地址',0,10),
('http-adapter','channel-test','bearer_token','Bearer Token',1,20),
('http-adapter','channel-test','timeout_seconds','超时秒',0,30);

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

INSERT INTO t_procurement_venues (id,name,city,lat,lng,address,partner_flag,keywords,active)
SELECT 'a1000000-0000-4000-8000-000000000001','心斋桥药妆合作点','大阪',34.6725000,135.5013000,'大阪市中央区心斋桥筋',1,'药妆,资生堂,红腰子,心斋桥',1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM t_procurement_venues WHERE id='a1000000-0000-4000-8000-000000000001');

INSERT INTO t_procurement_venues (id,name,city,lat,lng,address,partner_flag,keywords,active)
SELECT 'a1000000-0000-4000-8000-000000000002','难波药妆街','大阪',34.6652000,135.5023000,'大阪市浪速区难波',0,'药妆,难波,大阪',1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM t_procurement_venues WHERE id='a1000000-0000-4000-8000-000000000002');

INSERT INTO t_procurement_venues (id,name,city,lat,lng,address,partner_flag,keywords,active)
SELECT 'a1000000-0000-4000-8000-000000000003','关西机场免税店','大阪',34.4347000,135.2441000,'大阪府泉佐野市关西国际机场',1,'机场,免税,关西',1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM t_procurement_venues WHERE id='a1000000-0000-4000-8000-000000000003');

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