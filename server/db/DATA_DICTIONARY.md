# 数据库中文数据字典

数据库名：`suixinggou`。用途：随行带货交易撮合平台业务库，涵盖用户、商品、行程、订单、托管资金、店铺、保险、售后、配置与运营。MySQL不支持库级COMMENT；不重命名现有数据库。

范围：原有82表、提议新增31表，共113表/1186字段。仅补充备注，不改变表名、字段名、金额或业务数据；011另行提议将订单status扩宽至32字符。全部仍待真实环境完整验收。

增量版用于已有库升级，不包含原有82表的重建，也不会更新已经存在表的COMMENT；完整结构版含全部表，适用于新空库审阅初始化。原始上传SQL保持原样，带备注的原始82表副本另存reference/current-schema.annotated.sql。

给真实旧库补备注应根据其实际SHOW CREATE TABLE生成逐项审阅的专用迁移；本次不使用旧快照MODIFY覆盖线上字段属性，不执行任何SQL。


## `t_profiles` — 用户主档

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 主键，Spring Security 用户主键 |
| `username` | `username VARCHAR(64) NULL` | 用户名 |
| `openid` | `openid VARCHAR(128) NULL` | 微信 openid |
| `unionid` | `unionid VARCHAR(128) NULL` | 微信 unionid |
| `nickname` | `nickname VARCHAR(64) NULL` | 昵称 |
| `avatar_url` | `avatar_url VARCHAR(512) NULL` | 头像 URL |
| `phone` | `phone VARCHAR(32) NULL` | 手机号 |
| `city` | `city VARCHAR(64) NULL` | 常驻城市 |
| `is_admin` | `is_admin TINYINT(1) NOT NULL DEFAULT 0` | 是否运营账号 |
| `invite_code_used` | `invite_code_used VARCHAR(32) NULL` | 注册时使用的邀请码 |
| `carrier_verify_status` | `carrier_verify_status VARCHAR(16) NOT NULL DEFAULT 'none'` | 带货人实名认证审核状态，默认 none |
| `carrier_verify_reason` | `carrier_verify_reason VARCHAR(255) NULL` | 带货人认证驳回原因 |
| `verified_at` | `verified_at DATETIME(3) NULL` | 带货人认证通过时间 |
| `performance_score` | `performance_score INT NOT NULL DEFAULT 60` | 履约分 |
| `star_avg` | `star_avg DECIMAL(3,2) NOT NULL DEFAULT 5.00` | 平均星级 |
| `star_count` | `star_count INT NOT NULL DEFAULT 0` | 评价总数 |
| `deal_count` | `deal_count INT NOT NULL DEFAULT 0` | 累计成交单数 |
| `ontime_count` | `ontime_count INT NOT NULL DEFAULT 0` | 按时履约次数 |
| `breach_count_90d` | `breach_count_90d INT NOT NULL DEFAULT 0` | 近90天违约次数 |
| `abandon_count_90d` | `abandon_count_90d INT NOT NULL DEFAULT 0` | 近90天爽约次数 |
| `lost_dispute_count_90d` | `lost_dispute_count_90d INT NOT NULL DEFAULT 0` | 近90天败诉纠纷次数 |
| `clean_streak` | `clean_streak INT NOT NULL DEFAULT 0` | 连续无违规单数 |
| `dispute_reject_streak` | `dispute_reject_streak INT NOT NULL DEFAULT 0` | 纠纷连续驳回次数 |
| `banned_until` | `banned_until DATETIME(3) NULL` | 封禁截止时间 |
| `boost_factor` | `boost_factor DECIMAL(4,2) NOT NULL DEFAULT 1.00` | 流量加权系数 |
| `balance` | `balance DECIMAL(12,2) NOT NULL DEFAULT 0.00` | 钱包余额（元） |
| `payout_account` | `payout_account VARCHAR(128) NULL` | 用户提现收款账户，属于敏感账户信息 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `my_invite_code` | `my_invite_code VARCHAR(32) NULL` | 我的专属邀请码（唯一） |
| `invited_by` | `invited_by CHAR(36) NULL` | 邀请人 → t_profiles.id |
| `points` | `points INT NOT NULL DEFAULT 0` | 积分余额 |
| `real_name` | `real_name VARCHAR(64) NULL` | 用户实名姓名，属于个人敏感信息 |
| `id_card_no_masked` | `id_card_no_masked VARCHAR(32) NULL` | 脱敏身份证号码，不用于保存完整证件号 |
| `passport_no_masked` | `passport_no_masked VARCHAR(32) NULL` | 脱敏护照号码，不用于保存完整证件号 |
| `email` | `email VARCHAR(128) NULL` | 邮箱 |
| `phone_verified` | `phone_verified TINYINT(1) NOT NULL DEFAULT 0` | 手机号已验证 |
| `email_verified` | `email_verified TINYINT(1) NOT NULL DEFAULT 0` | 邮箱已验证 |
| `user_role` | `user_role VARCHAR(16) NOT NULL DEFAULT 'buyer'` | 注册身份 buyer/carrier/enterprise |
| `buyer_verify_status` | `buyer_verify_status VARCHAR(16) NOT NULL DEFAULT 'none'` | 买家轻量实名认证审核状态，默认 none |
| `buyer_verified_at` | `buyer_verified_at DATETIME(3) NULL` | 买家实名通过时间 |
| `carrier_tier` | `carrier_tier VARCHAR(16) NOT NULL DEFAULT 'casual'` | 带货人档位 casual/pro |
| `buyer_verify_reason` | `buyer_verify_reason VARCHAR(255) NULL` | 买家实名驳回原因 |
| `invite_tier` | `invite_tier VARCHAR(8) NOT NULL DEFAULT 'l0'` | 分销等级 l0~l4 |
| `valid_invitee_count` | `valid_invitee_count INT NOT NULL DEFAULT 0` | 有效邀请人数 |
| `commission_mtd` | `commission_mtd DECIMAL(12,2) NOT NULL DEFAULT 0.00` | 本月已计团队提成金额（元） |
| `commission_mtd_month` | `commission_mtd_month VARCHAR(7) NULL` | 团队提成统计月份（YYYY-MM） |
| `gender` | `gender VARCHAR(8) NULL` | 性别 |
| `notify_order` | `notify_order TINYINT(1) NOT NULL DEFAULT 1` | 通知偏好：订单动态 |
| `notify_logistics` | `notify_logistics TINYINT(1) NOT NULL DEFAULT 1` | 通知偏好：物流动态 |
| `notify_commission` | `notify_commission TINYINT(1) NOT NULL DEFAULT 1` | 通知偏好：提成到账 |
| `notify_system` | `notify_system TINYINT(1) NOT NULL DEFAULT 1` | 通知偏好：系统消息 |
| `consent_location` | `consent_location TINYINT(1) NOT NULL DEFAULT 1` | 隐私授权：定位 |
| `consent_camera` | `consent_camera TINYINT(1) NOT NULL DEFAULT 1` | 隐私授权：摄像头 |
| `consent_album` | `consent_album TINYINT(1) NOT NULL DEFAULT 1` | 隐私授权：相册 |
| `personalized_reco` | `personalized_reco TINYINT(1) NOT NULL DEFAULT 1` | 个性化推荐开关 |
| `deleted_at` | `deleted_at DATETIME(3) NULL` | 注销软删时间 |
| `consent_audio` | `consent_audio TINYINT(1) NOT NULL DEFAULT 1` | 隐私授权：录音 |
| `enterprise_name` | `enterprise_name VARCHAR(128) NULL` | 企业名称 |
| `business_license_no` | `business_license_no VARCHAR(64) NULL` | 统一社会信用代码 |
| `enterprise_verify_status` | `enterprise_verify_status VARCHAR(16) NOT NULL DEFAULT 'none'` | 企业账号认证审核状态，默认 none |

## `t_verification_codes` — 验证码与刷脸口令

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `target` | `target VARCHAR(128) NOT NULL` | 目标（手机号/邮箱/uid） |
| `channel` | `channel VARCHAR(32) NOT NULL` | 通道 sms/email/face_<scene> |
| `code` | `code VARCHAR(16) NOT NULL` | 验证码或刷脸口令内容，属于验证凭据，不应向无关用户披露 |
| `purpose` | `purpose VARCHAR(32) NOT NULL DEFAULT 'contact_verify'` | 验证码或刷脸口令的业务用途，默认 contact_verify |
| `expires_at` | `expires_at DATETIME(3) NOT NULL` | 过期时间 |
| `used` | `used TINYINT(1) NOT NULL DEFAULT 0` | 是否已消费 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 签发时间 |

## `t_kyc_documents` — 证件类 KYC 材料

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 上传人 |
| `doc_type` | `doc_type VARCHAR(32) NOT NULL` | 实名认证上传材料的类型标识 |
| `storage_url` | `storage_url VARCHAR(512) NOT NULL` | 存储桶 URL |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 实名认证材料审核状态，默认 pending |
| `reject_reason` | `reject_reason VARCHAR(255) NULL` | 驳回原因 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `challenge_code` | `challenge_code VARCHAR(32) NULL` | 上传认证材料时使用的挑战口令 |

## `t_face_verifications` — 刷脸留证记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 刷脸人 |
| `scene` | `scene VARCHAR(32) NOT NULL` | 刷脸留证的业务场景标识 |
| `snapshot_url` | `snapshot_url VARCHAR(512) NOT NULL` | 刷脸自拍留证图片地址，属于敏感认证材料 |
| `device_info` | `device_info VARCHAR(255) NULL` | 设备信息 |
| `result` | `result VARCHAR(16) NOT NULL DEFAULT 'passed'` | 刷脸留证结果标识，默认 passed |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `challenge_code` | `challenge_code VARCHAR(32) NULL` | 手写纸条口令 |

## `t_addresses` — 买家收货地址簿

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 所属用户 |
| `contact_name` | `contact_name VARCHAR(64) NOT NULL` | 联系人 |
| `contact_phone` | `contact_phone VARCHAR(32) NOT NULL` | 联系电话 |
| `province` | `province VARCHAR(64) NULL` | 省 |
| `city` | `city VARCHAR(64) NULL` | 市 |
| `district` | `district VARCHAR(64) NULL` | 区 |
| `detail` | `detail VARCHAR(512) NOT NULL` | 详细地址 |
| `is_default` | `is_default TINYINT(1) NOT NULL DEFAULT 0` | 是否默认地址 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |

## `t_pay_orders` — 微信支付充值单

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 充值用户 |
| `out_trade_no` | `out_trade_no VARCHAR(64) NOT NULL` | 商户订单号（唯一） |
| `prepay_id` | `prepay_id VARCHAR(128) NULL` | 微信预支付单号 |
| `transaction_id` | `transaction_id VARCHAR(128) NULL` | 微信支付流水号 |
| `amount_cents` | `amount_cents INT NOT NULL` | 微信充值金额（人民币分，整数） |
| `scene` | `scene VARCHAR(16) NOT NULL DEFAULT 'recharge'` | 场景 recharge |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 微信充值支付处理状态，具体流转以 PayOrders 支付处理代码为准 |
| `refund_no` | `refund_no VARCHAR(64) NULL` | 退款单号 |
| `paid_at` | `paid_at DATETIME(3) NULL` | 支付成功时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |

## `t_vc_delivery_outbox` — 验证码真实通道下发队列

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id BIGINT NOT NULL AUTO_INCREMENT` | 自增主键 |
| `target` | `target VARCHAR(128) NOT NULL` | 接收目标 |
| `channel` | `channel VARCHAR(16) NOT NULL` | 通道 phone/email |
| `code` | `code VARCHAR(16) NOT NULL` | 待发送验证码明文，属于临时验证凭据 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 验证码发送任务状态，具体流转以下发队列处理代码为准 |
| `error` | `error VARCHAR(255) NULL` | 验证码通道下发失败原因 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |

## `t_categories` — 三级品类树

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id BIGINT NOT NULL AUTO_INCREMENT` | 自增主键 |
| `name` | `name VARCHAR(128) NOT NULL` | 品类名称 |
| `icon` | `icon VARCHAR(64) NULL` | 图标类名字面量 |
| `ref_price_min` | `ref_price_min DECIMAL(12,2) NULL` | 参考价下限（元） |
| `ref_price_max` | `ref_price_max DECIMAL(12,2) NULL` | 参考价上限（元） |
| `region` | `region VARCHAR(64) NOT NULL DEFAULT '通用'` | 传统地区文案 |
| `limit_qty` | `limit_qty INT NULL` | 单次限购件数 |
| `need_permit` | `need_permit TINYINT(1) NOT NULL DEFAULT 0` | 是否需要许可证 |
| `forbidden_note` | `forbidden_note VARCHAR(512) NULL` | 禁运/限制提示 |
| `sort_order` | `sort_order INT NOT NULL DEFAULT 0` | 排序权重 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `parent_id` | `parent_id BIGINT NULL` | 父品类 → t_categories.id |
| `level` | `level INT NOT NULL DEFAULT 1` | 层级 1/2/3 |
| `active` | `active TINYINT(1) NOT NULL DEFAULT 1` | 是否启用 |

## `t_category_countries` — 品类-国家标签关联表（原 country text[]）

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `category_id` | `category_id BIGINT NOT NULL` | 品类 ID |
| `country_code` | `country_code VARCHAR(8) NOT NULL` | 国家/地区码 |

## `t_products` — 带货人在架商品

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `owner_id` | `owner_id CHAR(36) NOT NULL` | 上架带货人 |
| `category_id` | `category_id BIGINT NULL` | 品类 → t_categories.id |
| `name` | `name VARCHAR(255) NOT NULL` | 商品名 |
| `spec` | `spec VARCHAR(255) NULL` | 规格 |
| `region` | `region VARCHAR(64) NOT NULL DEFAULT '通用'` | 产地/版本地区 |
| `price` | `price DECIMAL(12,2) NOT NULL` | 售价（元） |
| `stock` | `stock INT NOT NULL DEFAULT 0` | 库存 |
| `image_urls` | `image_urls JSON NOT NULL` | 商品图片地址数组（JSON） |
| `note` | `note VARCHAR(1024) NULL` | 说明 |
| `status` | `status VARCHAR(8) NOT NULL DEFAULT 'on'` | 状态 on/off |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |

## `t_favorites` — 收藏通用表

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 收藏人 |
| `target_type` | `target_type VARCHAR(16) NOT NULL` | 目标类型 product/demand |
| `target_id` | `target_id CHAR(36) NOT NULL` | 目标ID |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_cart_items` — 购物车条目

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 用户 |
| `product_id` | `product_id CHAR(36) NOT NULL` | 商品 → t_products.id |
| `qty` | `qty INT NOT NULL DEFAULT 1` | 数量 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_trips` — 带货人发布的行程

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `owner_id` | `owner_id CHAR(36) NOT NULL` | 发布带货人 |
| `destination` | `destination VARCHAR(128) NOT NULL` | 目的地 |
| `destination_flag` | `destination_flag VARCHAR(16) NULL` | 目的地旗帜 emoji |
| `entry_port` | `entry_port VARCHAR(64) NULL` | 入境口岸 |
| `transport_mode` | `transport_mode VARCHAR(16) NOT NULL` | 交通方式 |
| `depart_date` | `depart_date DATE NOT NULL` | 出发日期 |
| `arrive_date` | `arrive_date DATE NULL` | 抵达日期 |
| `return_date` | `return_date DATE NOT NULL` | 返程/回国日期 |
| `capacity_qty` | `capacity_qty INT NOT NULL DEFAULT 20` | 可带件数容量 |
| `capacity_weight_kg` | `capacity_weight_kg DECIMAL(8,2) NOT NULL DEFAULT 20.00` | 可带重量（kg） |
| `fee_mode` | `fee_mode VARCHAR(16) NOT NULL DEFAULT 'percent'` | 行程带货费用的计费方式 |
| `fee_value` | `fee_value DECIMAL(8,2) NOT NULL DEFAULT 5.00` | 行程费率或费用值，计量方式随 fee_mode 确定 |
| `vendor_deposit_rate` | `vendor_deposit_rate DECIMAL(4,3) NOT NULL DEFAULT 0.200` | 带货人保证金比例（小数比例，0.200 表示20%） |
| `proof_urls` | `proof_urls JSON NOT NULL` | 行程资质凭证图片地址数组（JSON） |
| `verify_status` | `verify_status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 行程凭证审核状态 |
| `verify_reason` | `verify_reason VARCHAR(255) NULL` | 审核驳回原因 |
| `verified_at` | `verified_at DATETIME(3) NULL` | 审核通过时间 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'active'` | 行程当前业务状态 |
| `remaining_slots` | `remaining_slots INT NOT NULL DEFAULT 20` | 行程剩余可接单容量（件数） |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |
| `flight_no` | `flight_no VARCHAR(32) NULL` | 航班号 |
| `depart_datetime` | `depart_datetime DATETIME(3) NULL` | 起飞精确时刻 |
| `visa_proof_urls` | `visa_proof_urls JSON NOT NULL` | 签证凭证图片地址数组（JSON，属于认证材料） |
| `sms_proof_urls` | `sms_proof_urls JSON NOT NULL` | 行程确认短信凭证图片地址数组（JSON） |
| `boarding_snapshot_url` | `boarding_snapshot_url VARCHAR(512) NULL` | 登机口留证自拍 URL |
| `boarding_lat` | `boarding_lat DECIMAL(10,6) NULL` | 登机留证纬度 |
| `boarding_lng` | `boarding_lng DECIMAL(10,6) NULL` | 登机留证经度 |
| `boarding_city` | `boarding_city VARCHAR(64) NULL` | 登机留证城市 |
| `boarding_at` | `boarding_at DATETIME(3) NULL` | 登机留证时间 |
| `boarding_risk_flags` | `boarding_risk_flags JSON NOT NULL` | 登机留证校验产生的风险标记数组（JSON） |
| `product_ids` | `product_ids JSON NOT NULL` | 行程挂载的在架商品 ID 数组（JSON） |
| `trip_type` | `trip_type VARCHAR(16) NOT NULL DEFAULT 'international'` | 线路类型 |
| `direction` | `direction VARCHAR(16) NOT NULL DEFAULT 'outbound'` | 方向 |
| `origin_city` | `origin_city VARCHAR(64) NULL` | 出发城市 |
| `carry_mode` | `carry_mode VARCHAR(8) NOT NULL DEFAULT 'solo'` | 携带模式 |
| `pool_slots` | `pool_slots INT NULL` | 拼单池可用坑位数量 |
| `pool_status` | `pool_status VARCHAR(16) NOT NULL DEFAULT 'none'` | 行程拼单池的开放或成团状态 |
| `ticket_proof_urls` | `ticket_proof_urls JSON NOT NULL` | 车票或机票凭证图片地址数组（JSON） |
| `arrival_snapshot_url` | `arrival_snapshot_url VARCHAR(512) NULL` | 到城留证快照 |
| `arrival_city` | `arrival_city VARCHAR(64) NULL` | 到达城市 |
| `arrival_at` | `arrival_at DATETIME(3) NULL` | 到城留证时间 |
| `visibility` | `visibility VARCHAR(8) NOT NULL DEFAULT 'public'` | 行程的展示可见范围标识 |

## `t_trip_categories` — 行程-可带品类关联表（原 category_ids bigint[]）

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `trip_id` | `trip_id CHAR(36) NOT NULL` | 行程 ID |
| `category_id` | `category_id BIGINT NOT NULL` | 品类 ID |

## `t_demands` — 买家发布的求购单

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `title` | `title VARCHAR(255) NOT NULL` | 求购标题 |
| `item_desc` | `item_desc TEXT NULL` | 商品描述 |
| `image_urls` | `image_urls JSON NOT NULL` | 求购商品参考图片地址数组（JSON） |
| `expect_price` | `expect_price DECIMAL(12,2) NULL` | 期望价（元） |
| `expect_days` | `expect_days INT NULL` | 期望时效（天） |
| `publisher_id` | `publisher_id CHAR(36) NOT NULL` | 发布人 |
| `is_official` | `is_official TINYINT(1) NOT NULL DEFAULT 0` | 是否官方发单 |
| `deadline` | `deadline DATETIME(3) NULL` | 报价截止时间 |
| `status` | `status VARCHAR(24) NOT NULL DEFAULT 'open'` | 求购单当前发布或中标处理状态 |
| `awarded_quote_id` | `awarded_quote_id CHAR(36) NULL` | 中标报价 → t_quotes.id |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `pk_mode` | `pk_mode TINYINT(1) NOT NULL DEFAULT 1` | 是否 PK 盲报 |
| `pk_deadline` | `pk_deadline DATETIME(3) NULL` | PK 窗口截止 |
| `pk_result` | `pk_result JSON NULL` | PK 盲报报价评分快照（JSON），具体结构以 TradeRpc 和 TimeoutAndPayment 处理代码为准 |
| `trip_type` | `trip_type VARCHAR(16) NOT NULL DEFAULT 'any'` | 匹配线路 |
| `direction_pref` | `direction_pref VARCHAR(16) NOT NULL DEFAULT 'any'` | 方向偏好 |
| `city` | `city VARCHAR(64) NULL` | 需求城市 |
| `lat` | `lat DOUBLE NULL` | 需求纬度 |
| `lng` | `lng DOUBLE NULL` | 需求经度 |
| `tax_pref` | `tax_pref VARCHAR(32) NULL` | 退税偏好 |
| `insurance_pref` | `insurance_pref VARCHAR(16) NOT NULL DEFAULT 'unselected'` | 保价意向 |
| `shipping_bearer` | `shipping_bearer VARCHAR(16) NOT NULL DEFAULT 'negotiate'` | 邮费承担方 |
| `category_id` | `category_id BIGINT NULL` | 所属品类 |
| `visibility` | `visibility VARCHAR(8) NOT NULL DEFAULT 'public'` | 求购单的展示可见范围标识 |
| `matched_trip_id` | `matched_trip_id CHAR(36) NULL` | 匹配到的行程 |
| `revealed_at` | `revealed_at DATETIME(3) NULL` | 曝光时间 |
| `scheduled_at` | `scheduled_at DATETIME(3) NULL` | 定时发布时间 |

## `t_quotes` — 带货人对求购单的报价

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `demand_id` | `demand_id CHAR(36) NOT NULL` | 求购单 → t_demands.id |
| `vendor_id` | `vendor_id CHAR(36) NOT NULL` | 报价带货人 |
| `trip_id` | `trip_id CHAR(36) NULL` | 借线行程 |
| `price` | `price DECIMAL(12,2) NOT NULL` | 报价（元） |
| `promise_days` | `promise_days INT NOT NULL` | 承诺时效（天） |
| `purchase_place` | `purchase_place VARCHAR(255) NULL` | 采购地 |
| `note` | `note VARCHAR(1024) NULL` | 说明 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'submitted'` | 带货人报价的当前处理状态 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_publish_deposits` — 发布保证金

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 缴纳人 |
| `amount` | `amount DECIMAL(12,2) NOT NULL` | 发布求购或意向时冻结的保证金金额（元） |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'frozen'` | 发布保证金的冻结或退回处理状态 |
| `source` | `source VARCHAR(16) NOT NULL DEFAULT 'demand'` | 来源 demand/intent |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单 |
| `reason` | `reason VARCHAR(255) NULL` | 说明 |
| `refunded_at` | `refunded_at DATETIME(3) NULL` | 退回时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |

## `t_orders` — 订单主表

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_no` | `order_no VARCHAR(64) NOT NULL` | 订单号（唯一） |
| `trip_id` | `trip_id CHAR(36) NULL` | 关联行程 |
| `buyer_id` | `buyer_id CHAR(36) NOT NULL` | 买家 |
| `vendor_id` | `vendor_id CHAR(36) NULL` | 带货人 |
| `demand_id` | `demand_id CHAR(36) NULL` | 来源求购单 |
| `source` | `source VARCHAR(16) NOT NULL DEFAULT 'trip_intent'` | 成单来源 |
| `item_name` | `item_name VARCHAR(255) NOT NULL` | 商品名称 |
| `item_spec` | `item_spec VARCHAR(255) NULL` | 规格 |
| `quantity` | `quantity INT NOT NULL DEFAULT 1` | 购买数量 |
| `unit` | `unit VARCHAR(16) NOT NULL DEFAULT '件'` | 计量单位 |
| `expect_unit_price` | `expect_unit_price DECIMAL(12,2) NOT NULL` | 期望单价（元） |
| `goods_amount` | `goods_amount DECIMAL(12,2) NOT NULL` | 货款总额（元） |
| `service_fee` | `service_fee DECIMAL(12,2) NOT NULL` | 平台服务费金额（元） |
| `buyer_deposit` | `buyer_deposit DECIMAL(12,2) NOT NULL` | 买家冻结资金金额（元，实际组成由 agreed_json 定价版本快照确定） |
| `vendor_deposit` | `vendor_deposit DECIMAL(12,2) NOT NULL DEFAULT 0.00` | 带货人履约保证金金额（元） |
| `final_price` | `final_price DECIMAL(12,2) NULL` | 订单最终成交金额（元） |
| `ref_image_urls` | `ref_image_urls JSON NOT NULL` | 订单商品参考图片地址数组（JSON） |
| `expect_arrive_date` | `expect_arrive_date DATE NULL` | 买家期望送达日 |
| `promised_arrive_date` | `promised_arrive_date DATE NULL` | 带货人承诺送达日 |
| `contact_name` | `contact_name VARCHAR(64) NULL` | 收货联系人 |
| `contact_phone` | `contact_phone VARCHAR(32) NULL` | 收货电话 |
| `address` | `address VARCHAR(512) NULL` | 收货地址 |
| `note` | `note VARCHAR(1024) NULL` | 买家备注 |
| `status` | `status VARCHAR(32) NOT NULL DEFAULT 'requesting'（011后；原始为16字符）` | 订单状态机 |
| `buyer_deposit_status` | `buyer_deposit_status VARCHAR(16) NOT NULL DEFAULT 'frozen'` | 买家冻结资金的缴纳、冻结及释放状态 |
| `vendor_deposit_status` | `vendor_deposit_status VARCHAR(16) NOT NULL DEFAULT 'unpaid'` | 带货人履约保证金的缴纳、冻结及释放状态 |
| `negotiation_quote` | `negotiation_quote DECIMAL(12,2) NULL` | 带货人提出的议价金额（元） |
| `negotiation_note` | `negotiation_note VARCHAR(1024) NULL` | 议价说明 |
| `negotiation_deadline` | `negotiation_deadline DATETIME(3) NULL` | 议价截止 |
| `intent_deadline` | `intent_deadline DATETIME(3) NULL` | 意向金有效期 |
| `vendor_deposit_deadline` | `vendor_deposit_deadline DATETIME(3) NULL` | 补缴保证金截止 |
| `purchase_receipt_urls` | `purchase_receipt_urls JSON NOT NULL` | 采购凭证图片地址汇总数组（JSON） |
| `carrier_code` | `carrier_code VARCHAR(32) NULL` | 快递公司编码 |
| `tracking_no` | `tracking_no VARCHAR(64) NULL` | 快递单号 |
| `shipped_at` | `shipped_at DATETIME(3) NULL` | 寄出时间 |
| `auto_confirm_at` | `auto_confirm_at DATETIME(3) NULL` | 自动确认收货时间 |
| `agreed_json` | `agreed_json JSON NULL` | 订单约定及定价快照（JSON），含定价版本、资金组成和保险选择等信息 |
| `cancel_reason_type` | `cancel_reason_type VARCHAR(32) NULL` | 取消原因类型 |
| `cancel_force_majeure` | `cancel_force_majeure TINYINT(1) NULL` | 是否主张不可抗力 |
| `cancel_proof_urls` | `cancel_proof_urls JSON NOT NULL` | 取消订单申请的举证图片地址数组（JSON） |
| `cancel_penalty_amount` | `cancel_penalty_amount DECIMAL(12,2) NULL` | 取消订单裁定的违约金金额（元） |
| `cancel_withdraw_used` | `cancel_withdraw_used TINYINT(1) NOT NULL DEFAULT 0` | 是否已使用取消申请的撤回机会 |
| `closed_reason` | `closed_reason VARCHAR(255) NULL` | 关闭原因 |
| `settled_at` | `settled_at DATETIME(3) NULL` | 结算完成时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |
| `support_state` | `support_state VARCHAR(16) NOT NULL DEFAULT 'none'` | 订单请求平台介入的处理状态 |
| `support_ticket_id` | `support_ticket_id CHAR(36) NULL` | 关联工单 |
| `purchase_evidence` | `purchase_evidence JSON NOT NULL` | 采购举证对象（JSON）：receipt 小票、goods 商品、storefront 门店图片数组，可含 tax_refund_receipt 退税凭证 |
| `procurement_lat` | `procurement_lat DECIMAL(10,6) NULL` | 采购地纬度 |
| `procurement_lng` | `procurement_lng DECIMAL(10,6) NULL` | 采购地经度 |
| `procurement_address` | `procurement_address VARCHAR(512) NULL` | 采购地地址 |
| `procurement_city` | `procurement_city VARCHAR(64) NULL` | 采购城市 |
| `procurement_at` | `procurement_at DATETIME(3) NULL` | 采购确认时间 |
| `purchase_face_verification_id` | `purchase_face_verification_id CHAR(36) NULL` | 采购确认刷脸记录 |
| `location_mismatch` | `location_mismatch TINYINT(1) NOT NULL DEFAULT 0` | 采购城市与行程不符标记 |
| `shipping_fee` | `shipping_fee DECIMAL(12,2) NULL` | 国内配送邮费金额（元） |
| `shipping_fee_note` | `shipping_fee_note VARCHAR(1024) NULL` | 邮费说明 |
| `shipping_fee_status` | `shipping_fee_status VARCHAR(16) NOT NULL DEFAULT 'none'` | 国内邮费报价协商的当前状态 |
| `shipping_fee_deadline` | `shipping_fee_deadline DATETIME(3) NULL` | 邮费协商截止 |
| `freight_label_urls` | `freight_label_urls JSON NOT NULL` | 快递面单图片地址数组（JSON，可能含收寄件个人信息） |
| `ship_lat` | `ship_lat DECIMAL(10,6) NULL` | 寄出地纬度 |
| `ship_lng` | `ship_lng DECIMAL(10,6) NULL` | 寄出地经度 |
| `ship_city` | `ship_city VARCHAR(64) NULL` | 寄出城市 |
| `ship_address` | `ship_address VARCHAR(512) NULL` | 寄出地址 |
| `ship_risk_flags` | `ship_risk_flags JSON NOT NULL` | 寄出或面交校验产生的风险标记数组（JSON） |
| `purchase_meta` | `purchase_meta JSON NOT NULL` | 采购元信息对象（JSON），含 date_str 日期条和 order_tail 订单尾号校验信息 |
| `purchase_risk_flags` | `purchase_risk_flags JSON NOT NULL` | 采购地点与行程匹配校验产生的风险标记数组（JSON） |
| `confirmed_at` | `confirmed_at DATETIME(3) NULL` | 买家确认收货时间 |
| `cancel_tier` | `cancel_tier VARCHAR(16) NULL` | 取消定责档位快照 |
| `pool_slot_no` | `pool_slot_no INT NULL` | 拼单坑位序号 |
| `tax_refund_plan` | `tax_refund_plan VARCHAR(32) NULL` | 退税方案 |
| `tax_rate` | `tax_rate DECIMAL(5,4) NULL` | 退税计算比例（小数比例，如0.08表示8%） |
| `tax_refund_amount` | `tax_refund_amount DECIMAL(12,2) NULL` | 退税金额（元） |
| `delivery_mode` | `delivery_mode VARCHAR(16) NOT NULL DEFAULT 'ship'` | 交付方式 |
| `meetup_code` | `meetup_code VARCHAR(8) NULL` | 面交核销码 |
| `meetup_confirmed_json` | `meetup_confirmed_json JSON NULL` | 面交核销确认快照（JSON），记录买家 ID、确认时间、经纬度与城市 |
| `insured` | `insured VARCHAR(16) NOT NULL DEFAULT 'unselected'` | 订单保价或保险选择状态，实际承保以保险处理记录为准 |
| `insurance_premium` | `insurance_premium DECIMAL(12,2) NOT NULL DEFAULT 0.00` | 历史订单保费金额（CNY元）；机构直付版本不托管该费用，历史非零值需核对 |
| `declared_value` | `declared_value DECIMAL(12,2) NULL` | 商品申报价值金额（元） |
| `delivery_face_verification_id` | `delivery_face_verification_id CHAR(36) NULL` | 交付刷脸记录 |
| `delivery_lat` | `delivery_lat DECIMAL(10,6) NULL` | 交付地点纬度 |
| `delivery_lng` | `delivery_lng DECIMAL(10,6) NULL` | 交付地点经度 |
| `delivery_city` | `delivery_city VARCHAR(64) NULL` | 交付城市 |
| `delivery_at` | `delivery_at DATETIME(3) NULL` | 交付时间 |

## `t_deposits` — 订单保证金流水台账

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 订单 → t_orders.id |
| `payer_id` | `payer_id CHAR(36) NOT NULL` | 缴纳人 |
| `party_role` | `party_role VARCHAR(16) NOT NULL` | 缴纳方角色 buyer/vendor |
| `kind` | `kind VARCHAR(16) NOT NULL` | 保证金类型 |
| `amount` | `amount DECIMAL(12,2) NOT NULL` | 金额（元） |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'frozen'` | 状态 |
| `payee_id` | `payee_id CHAR(36) NULL` | 收款方 |
| `reason` | `reason VARCHAR(255) NULL` | 变动原因 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 更新时间 |

## `t_order_logistics` — 订单物流节点轨迹

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 订单 |
| `node` | `node VARCHAR(32) NOT NULL` | 物流节点 |
| `operator_id` | `operator_id CHAR(36) NULL` | 操作人 |
| `operator_role` | `operator_role VARCHAR(16) NOT NULL DEFAULT 'vendor'` | 操作角色 |
| `detail` | `detail VARCHAR(1024) NULL` | 节点说明 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 记录时间 |

## `t_cancel_requests` — 取消申请与定责

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 订单 |
| `applicant_id` | `applicant_id CHAR(36) NOT NULL` | 申请人 |
| `applicant_role` | `applicant_role VARCHAR(16) NOT NULL` | 申请角色 buyer/vendor |
| `reason_type` | `reason_type VARCHAR(32) NOT NULL` | 原因类型 |
| `reason_detail` | `reason_detail VARCHAR(1024) NULL` | 原因详情 |
| `proof_urls` | `proof_urls JSON NOT NULL` | 取消申请举证图片地址数组（JSON） |
| `claim_force_majeure` | `claim_force_majeure TINYINT(1) NOT NULL DEFAULT 0` | 是否主张不可抗力 |
| `mutual_plan` | `mutual_plan JSON NULL` | 双方协商的取消处理方案（JSON），具体结构以取消申请处理代码为准 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'reviewing'` | 取消申请当前受理或裁定状态 |
| `verdict` | `verdict VARCHAR(16) NULL` | 定责结论 |
| `penalty_amount` | `penalty_amount DECIMAL(12,2) NULL` | 取消定责裁定违约金金额（元） |
| `verdict_reason` | `verdict_reason VARCHAR(1024) NULL` | 裁定理由 |
| `withdrawn` | `withdrawn TINYINT(1) NOT NULL DEFAULT 0` | 是否撤回 |
| `reviewed_by` | `reviewed_by CHAR(36) NULL` | 核责运营 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `resolved_at` | `resolved_at DATETIME(3) NULL` | 裁定时间 |

## `t_disputes` — 纠纷工单

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 订单 |
| `initiator_id` | `initiator_id CHAR(36) NOT NULL` | 发起方 |
| `respondent_id` | `respondent_id CHAR(36) NULL` | 被诉方 |
| `type` | `type VARCHAR(32) NOT NULL` | 纠纷类型 |
| `expect_resolution` | `expect_resolution VARCHAR(64) NOT NULL` | 期望处理方式 |
| `description` | `description TEXT NOT NULL` | 问题描述 |
| `evidence_urls` | `evidence_urls JSON NOT NULL` | 纠纷发起方举证图片地址数组（JSON） |
| `respondent_evidence_urls` | `respondent_evidence_urls JSON NOT NULL` | 纠纷被诉方举证图片地址数组（JSON） |
| `respondent_note` | `respondent_note VARCHAR(1024) NULL` | 被诉方说明 |
| `stage` | `stage VARCHAR(24) NOT NULL DEFAULT 'negotiating'` | 纠纷当前协商或裁决处理阶段 |
| `round` | `round INT NOT NULL DEFAULT 1` | 纠纷当前协商轮次数 |
| `verdict` | `verdict VARCHAR(64) NULL` | 裁决结果 |
| `refund_amount` | `refund_amount DECIMAL(12,2) NULL` | 纠纷裁定退款金额（元） |
| `deposit_owner` | `deposit_owner VARCHAR(16) NULL` | 保证金归属判定 |
| `verdict_reason` | `verdict_reason VARCHAR(1024) NULL` | 裁决理由 |
| `reviewed_by` | `reviewed_by CHAR(36) NULL` | 裁决运营 |
| `reopened_used` | `reopened_used TINYINT(1) NOT NULL DEFAULT 0` | 是否已用过一次复议 |
| `review_appeal` | `review_appeal TEXT NULL` | 复议申诉内容 |
| `review_result` | `review_result VARCHAR(64) NULL` | 复议结果 |
| `negotiation_deadline` | `negotiation_deadline DATETIME(3) NULL` | 协商截止 |
| `resolved_at` | `resolved_at DATETIME(3) NULL` | 解决时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_reviews` — 订单完成后的互评

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 订单 |
| `reviewer_id` | `reviewer_id CHAR(36) NOT NULL` | 评价人 |
| `target_id` | `target_id CHAR(36) NOT NULL` | 被评价人 |
| `stars` | `stars INT NOT NULL DEFAULT 5` | 星级 1-5 |
| `tags` | `tags JSON NOT NULL` | 订单评价标签文本数组（JSON） |
| `content` | `content TEXT NULL` | 文字评价 |
| `hidden` | `hidden TINYINT(1) NOT NULL DEFAULT 0` | 是否隐藏 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_balance_ledger` — 钱包余额总账

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 入账用户 |
| `kind` | `kind VARCHAR(16) NOT NULL` | 流水类型 |
| `amount` | `amount DECIMAL(12,2) NOT NULL` | 变动金额（±，元） |
| `balance_after` | `balance_after DECIMAL(12,2) NULL` | 该笔流水落账后的钱包余额快照（元） |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单 |
| `note` | `note VARCHAR(255) NULL` | 备注 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 落账时间 |

## `t_invoices` — 电子发票申请与开具

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 订单（每单一票） |
| `user_id` | `user_id CHAR(36) NOT NULL` | 申请人 |
| `title_type` | `title_type VARCHAR(16) NOT NULL DEFAULT 'personal'` | 抬头类型 |
| `title_name` | `title_name VARCHAR(128) NOT NULL` | 抬头名称 |
| `tax_no` | `tax_no VARCHAR(64) NULL` | 纳税人识别号 |
| `invoice_kind` | `invoice_kind VARCHAR(32) NOT NULL DEFAULT 'electronic_general'` | 发票种类 |
| `amount` | `amount DECIMAL(12,2) NOT NULL` | 申请开具发票的金额（元） |
| `email` | `email VARCHAR(128) NOT NULL` | 接收邮箱 |
| `remark` | `remark VARCHAR(1024) NULL` | 备注 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 电子发票申请审核或开具状态 |
| `invoice_no` | `invoice_no VARCHAR(64) NULL` | 发票号 |
| `file_url` | `file_url VARCHAR(512) NULL` | 发票文件 URL |
| `issued_at` | `issued_at DATETIME(3) NULL` | 开具时间 |
| `reject_reason` | `reject_reason VARCHAR(255) NULL` | 驳回原因 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 申请时间 |

## `t_coupons` — 优惠券/积分兑换券

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 持有人 |
| `face_value` | `face_value DECIMAL(10,2) NOT NULL` | 面值（元） |
| `points_cost` | `points_cost INT NOT NULL` | 积分兑换分值 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'active'` | 优惠券当前可用、核销或过期状态 |
| `used_order_id` | `used_order_id CHAR(36) NULL` | 核销订单 |
| `expire_at` | `expire_at DATETIME(3) NOT NULL` | 过期时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_invite_codes` — 平台邀请码池

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `code` | `code VARCHAR(32) NOT NULL` | 邀请码（主键） |
| `note` | `note VARCHAR(255) NULL` | 备注 |
| `used_by` | `used_by CHAR(36) NULL` | 使用者 → t_profiles.id |
| `used_at` | `used_at DATETIME(3) NULL` | 使用时间 |
| `created_by` | `created_by CHAR(36) NULL` | 创建运营 → t_profiles.id |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_reward_ledger` — 分销奖励台账（双币种）

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `inviter_id` | `inviter_id CHAR(36) NOT NULL` | 受益邀请人 |
| `invitee_id` | `invitee_id CHAR(36) NOT NULL` | 被邀请人 |
| `order_id` | `order_id CHAR(36) NULL` | 计佣订单（首单奖励为 NULL） |
| `level` | `level SMALLINT NOT NULL` | 层级 1-4 |
| `event_type` | `event_type VARCHAR(32) NOT NULL` | 事件类型 |
| `reward_kind` | `reward_kind VARCHAR(16) NOT NULL DEFAULT 'points'` | 奖励种类 |
| `amount` | `amount INT NOT NULL` | 数额（cny 存分；points 存分值） |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'settled'` | 奖励记录的结算或撤销状态 |
| `note` | `note VARCHAR(255) NULL` | 说明 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `rate` | `rate DECIMAL(6,4) NULL` | 团队提成计算比例快照（小数比例） |
| `base_amount` | `base_amount DECIMAL(12,2) NULL` | 计算团队提成时采用的平台服务费基数（元） |
| `currency` | `currency VARCHAR(8) NOT NULL DEFAULT 'points'` | 币种 points/cny |
| `settled_to_balance` | `settled_to_balance TINYINT(1) NOT NULL DEFAULT 0` | 该笔现金奖励是否已计入用户钱包余额 |
| `revoked_at` | `revoked_at DATETIME(3) NULL` | 撤销时间 |
| `revoked_reason` | `revoked_reason VARCHAR(255) NULL` | 撤销原因 |

## `t_score_logs` — 履约分变动日志

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 用户 |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单 |
| `event` | `event VARCHAR(64) NOT NULL` | 触发事件 |
| `delta` | `delta INT NOT NULL` | 分值增减 |
| `score_after` | `score_after INT NOT NULL` | 变动后总分 |
| `note` | `note VARCHAR(255) NULL` | 说明 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_notifications` — 站内通知

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | UUID 主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 接收人 |
| `kind` | `kind VARCHAR(32) NOT NULL` | 通知类型 |
| `title` | `title VARCHAR(255) NOT NULL` | 标题 |
| `body` | `body TEXT NULL` | 正文 |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单 |
| `read_flag` | `read_flag TINYINT(1) NOT NULL DEFAULT 0` | 已读 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |

## `t_notify_outbox` — 微信订阅消息待发送队列及发送结果

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 消息队列记录UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 接收用户ID |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单ID |
| `kind` | `kind VARCHAR(32) NOT NULL` | 订阅消息模板类型 |
| `title` | `title VARCHAR(255) NOT NULL` | 消息标题 |
| `body` | `body TEXT NULL` | 消息正文 |
| `channel` | `channel VARCHAR(32) NOT NULL DEFAULT 'wechat_subscribe'` | 消息发送通道，默认wechat_subscribe |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 消息发送状态，默认pending待发送 |
| `reason` | `reason VARCHAR(255) NULL` | 发送失败原因 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 消息入队时间 |
| `sent_at` | `sent_at DATETIME(3) NULL` | 消息发送时间 |

## `t_messages` — 订单参与方会话留言及图片消息

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 订单留言UUID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 所属订单ID |
| `sender_id` | `sender_id CHAR(36) NOT NULL` | 发送用户ID |
| `content` | `content TEXT NOT NULL` | 留言正文 |
| `image_urls` | `image_urls JSON NOT NULL` | 留言图片URL列表，JSON数组 |
| `system_flag` | `system_flag TINYINT(1) NOT NULL DEFAULT 0` | 是否系统消息：1是、0否 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 留言创建时间 |
| `sender_role` | `sender_role VARCHAR(16) NOT NULL DEFAULT 'normal'` | 留言发送角色，默认normal |

## `t_support_threads` — 客服、AI及好友私聊会话线程

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 会话线程UUID |
| `kind` | `kind VARCHAR(16) NOT NULL` | 会话类型：ai人工智能、human人工客服、peer用户私聊 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 会话发起用户ID |
| `peer_id` | `peer_id CHAR(36) NULL` | 私聊对端用户ID，适用于peer会话 |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单ID |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'open'` | 线程状态，默认open |
| `last_message_at` | `last_message_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 最近一条消息时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 会话创建时间 |

## `t_support_tickets` — 旧版客服工单及客服处理回复

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 客服工单UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 工单发起用户ID |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单ID |
| `topic` | `topic VARCHAR(255) NOT NULL` | 工单主题 |
| `content` | `content TEXT NOT NULL` | 工单问题内容 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'open'` | 工单处理状态，默认open |
| `handler_id` | `handler_id CHAR(36) NULL` | 处理客服用户ID |
| `reply` | `reply TEXT NULL` | 客服回复内容 |
| `last_reply_at` | `last_reply_at DATETIME(3) NULL` | 最近回复时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 工单创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 工单更新时间 |

## `t_support_messages` — 客服及私聊会话线程中的消息

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 会话消息UUID |
| `thread_id` | `thread_id CHAR(36) NOT NULL` | 所属会话线程ID，关联t_support_threads.id |
| `sender_id` | `sender_id CHAR(36) NULL` | 发送用户ID，系统消息可为空 |
| `sender_role` | `sender_role VARCHAR(16) NOT NULL DEFAULT 'user'` | 消息发送角色，默认user |
| `content` | `content TEXT NOT NULL` | 消息正文 |
| `image_urls` | `image_urls JSON NOT NULL` | 消息图片URL列表，JSON数组 |
| `read_flag` | `read_flag TINYINT(1) NOT NULL DEFAULT 0` | 是否已读：1已读、0未读 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 消息创建时间 |

## `t_chat_violations` — 聊天防引流规则命中及人工复核记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 违规记录UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 命中违规规则的用户ID |
| `code` | `code VARCHAR(64) NOT NULL` | 命中的防引流规则编码 |
| `snippet` | `snippet VARCHAR(512) NULL` | 命中规则的聊天文本片段 |
| `reviewed` | `reviewed TINYINT(1) NOT NULL DEFAULT 0` | 是否完成人工复核：1是、0否 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 违规留痕时间 |

## `t_call_records` — 网络语音通话及录音知情确认记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 通话记录UUID |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单ID |
| `caller_id` | `caller_id CHAR(36) NOT NULL` | 主叫用户ID |
| `callee_id` | `callee_id CHAR(36) NOT NULL` | 被叫用户ID |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'ringing'` | 通话状态，默认ringing响铃中 |
| `room_id` | `room_id VARCHAR(64) NULL` | RTC通话房间编号 |
| `started_at` | `started_at DATETIME(3) NULL` | 通话接通时间 |
| `ended_at` | `ended_at DATETIME(3) NULL` | 通话挂断时间 |
| `duration_sec` | `duration_sec INT NOT NULL DEFAULT 0` | 通话时长，单位秒 |
| `recording_url` | `recording_url VARCHAR(512) NULL` | 录音存档URL |
| `transcript_flags` | `transcript_flags JSON NOT NULL` | 语音转写风险词标记列表，JSON数组 |
| `consent_confirmed` | `consent_confirmed TINYINT(1) NOT NULL DEFAULT 0` | 双方是否确认录音知情：1是、0否 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 通话记录创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 通话记录更新时间 |

## `t_privacy_numbers` — 隐私通话虚拟中间号码资源池

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `virtual_number` | `virtual_number VARCHAR(32) NOT NULL` | 虚拟中间号码，主键 |
| `pool_code` | `pool_code VARCHAR(32) NOT NULL DEFAULT 'default'` | 所属号码池编码，默认default |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'idle'` | 号码资源状态，默认idle空闲 |
| `remark` | `remark VARCHAR(255) NULL` | 号码资源备注 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 号码入池时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 号码信息更新时间 |

## `t_privacy_bindings` — 订单双方隐私通话虚拟号码绑定及释放记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 虚拟号绑定记录UUID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `virtual_number` | `virtual_number VARCHAR(32) NOT NULL` | 绑定占用的虚拟中间号码 |
| `buyer_phone_masked` | `buyer_phone_masked VARCHAR(32) NULL` | 脱敏后的买家电话号码，不存储完整明文号码 |
| `vendor_phone_masked` | `vendor_phone_masked VARCHAR(32) NULL` | 脱敏后的带货人电话号码，不存储完整明文号码 |
| `aliyun_sub_id` | `aliyun_sub_id VARCHAR(64) NULL` | 运营商返回的号码绑定标识 |
| `expire_at` | `expire_at DATETIME(3) NOT NULL` | 号码绑定到期时间 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'active'` | 绑定状态，默认active有效 |
| `release_reason` | `release_reason VARCHAR(255) NULL` | 号码绑定释放原因 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 绑定创建时间 |
| `released_at` | `released_at DATETIME(3) NULL` | 绑定实际释放时间 |

## `t_call_logs` — 通过订单隐私虚拟号发起的拨打记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 虚拟号拨打记录UUID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `binding_id` | `binding_id CHAR(36) NULL` | 关联虚拟号绑定记录ID |
| `caller_id` | `caller_id CHAR(36) NOT NULL` | 主叫用户ID |
| `caller_role` | `caller_role VARCHAR(16) NOT NULL` | 主叫方在订单中的角色 |
| `peer_role` | `peer_role VARCHAR(16) NOT NULL` | 被叫方在订单中的角色 |
| `virtual_number` | `virtual_number VARCHAR(32) NOT NULL` | 本次拨打使用的虚拟号码 |
| `result` | `result VARCHAR(16) NOT NULL DEFAULT 'dialed'` | 拨打结果，默认dialed已发起拨打 |
| `duration_sec` | `duration_sec INT NULL` | 通话时长，单位秒 |
| `note` | `note VARCHAR(255) NULL` | 拨打记录备注 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 拨打记录创建时间 |

## `t_admin_logs` — 运营后台操作审计留痕

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 后台操作记录UUID |
| `operator_id` | `operator_id CHAR(36) NULL` | 执行操作的运营用户ID |
| `action` | `action VARCHAR(64) NOT NULL` | 后台操作类型编码 |
| `target_type` | `target_type VARCHAR(32) NULL` | 被操作对象类型 |
| `target_id` | `target_id VARCHAR(64) NULL` | 被操作对象标识 |
| `detail` | `detail TEXT NULL` | 后台操作详情 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 操作留痕时间 |

## `t_legal_docs` — 法律协议及说明文档的正文和版本

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 法律文档UUID |
| `doc_key` | `doc_key VARCHAR(64) NOT NULL` | 文档唯一业务键 |
| `title` | `title VARCHAR(255) NOT NULL` | 文档标题 |
| `body_md` | `body_md MEDIUMTEXT NOT NULL` | Markdown格式文档正文 |
| `version` | `version VARCHAR(16) NOT NULL DEFAULT 'v1.0'` | 文档版本号，默认v1.0 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 文档最近更新时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 文档创建时间 |

## `t_join_applications` — 运营账号加入申请与处理记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL` | 加入申请UUID |
| `operator_id` | `operator_id CHAR(36) NOT NULL` | 提交申请的运营用户ID |
| `target_username` | `target_username VARCHAR(64) NOT NULL` | 申请目标账号用户名 |
| `target_user_id` | `target_user_id CHAR(36) NULL` | 申请目标用户ID |
| `note` | `note VARCHAR(1024) NULL` | 加入申请说明 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 申请处理状态，默认pending待处理 |
| `responded_at` | `responded_at DATETIME(3) NULL` | 申请处理时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 申请创建时间 |

## `t_social_posts` — 门户社区动态内容及审核记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 社区动态UUID |
| `author_id` | `author_id CHAR(36) NOT NULL` | 动态发布用户ID |
| `caption` | `caption VARCHAR(4000) NOT NULL` | 动态文字内容 |
| `media_kind` | `media_kind VARCHAR(12) NOT NULL DEFAULT 'text'` | 媒体类型，默认text纯文字 |
| `media_url` | `media_url VARCHAR(800)` | 动态媒体资源URL |
| `poster_url` | `poster_url VARCHAR(800)` | 动态媒体封面URL |
| `status` | `status VARCHAR(24) NOT NULL DEFAULT 'pending_review'` | 内容审核状态：pending_review待审核、approved通过、rejected拒绝 |
| `reviewer_id` | `reviewer_id CHAR(36)` | 审核运营用户ID |
| `reviewed_at` | `reviewed_at DATETIME` | 审核完成时间 |
| `created_at` | `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP` | 动态创建时间 |

## `t_support_conversations` — 用户与AI及人工客服的接待会话

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 客服接待会话UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 发起会话的用户ID |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'ai'` | 接待状态：ai智能客服、waiting等待人工、human人工接待 |
| `assigned_agent_id` | `assigned_agent_id CHAR(36)` | 已分配人工客服用户ID |
| `created_at` | `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP` | 接待会话创建时间 |
| `updated_at` | `updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 接待会话更新时间 |

## `t_support_conversation_messages` — 客服接待会话的往来消息

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id BIGINT PRIMARY KEY AUTO_INCREMENT` | 自增消息主键 |
| `conversation_id` | `conversation_id CHAR(36) NOT NULL` | 所属客服接待会话ID |
| `sender_type` | `sender_type VARCHAR(16) NOT NULL` | 消息发送方类型 |
| `sender_id` | `sender_id CHAR(36)` | 发送方用户ID，非用户发送时可为空 |
| `content` | `content VARCHAR(5000) NOT NULL` | 客服会话消息正文 |
| `created_at` | `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP` | 消息发送时间 |

## `t_portal_demand_reviews` — 门户需求发布内容审核记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `demand_id` | `demand_id CHAR(36) PRIMARY KEY` | 待审核需求ID，主键 |
| `author_id` | `author_id CHAR(36) NOT NULL` | 需求发布用户ID |
| `status` | `status VARCHAR(24) NOT NULL DEFAULT 'pending_review'` | 需求审核状态：pending_review待审核、approved通过、rejected拒绝 |
| `reviewer_id` | `reviewer_id CHAR(36)` | 审核运营用户ID |
| `reviewed_at` | `reviewed_at DATETIME` | 需求审核完成时间 |
| `created_at` | `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP` | 审核记录创建时间 |

## `t_user_preferences` — 用户语言、地区、币种及时区偏好

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `user_id` | `user_id CHAR(36) PRIMARY KEY` | 用户ID，主键 |
| `locale` | `locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN'` | 界面语言地区代码，默认zh-CN |
| `country_code` | `country_code CHAR(2) NOT NULL DEFAULT 'CN'` | 国家或地区代码，默认CN |
| `currency_code` | `currency_code CHAR(3) NOT NULL DEFAULT 'CNY'` | 偏好币种代码，默认CNY |
| `time_zone` | `time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai'` | 用户时区标识，默认Asia/Shanghai |

## `t_revoked_tokens` — 已撤销认证令牌摘要及失效时间

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `token_hash` | `token_hash CHAR(64) PRIMARY KEY` | 已撤销令牌的摘要，主键；不保存令牌明文 |
| `expires_at` | `expires_at DATETIME NOT NULL` | 令牌原定过期时间 |
| `revoked_at` | `revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 令牌撤销时间 |

## `t_account_deletion_requests` — 用户账号注销申请及运营处理记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 账号注销申请UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 申请注销的用户ID |
| `reason` | `reason VARCHAR(1000)` | 用户填写的注销原因 |
| `status` | `status VARCHAR(24) NOT NULL DEFAULT 'submitted'` | 注销申请处理状态，默认submitted已提交 |
| `operator_note` | `operator_note VARCHAR(1000)` | 运营处理备注 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 注销申请创建时间 |
| `updated_at` | `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 注销申请更新时间 |

## `t_cbec_product_reviews` — 跨境电商商品合规资料及审核结论

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `product_id` | `product_id CHAR(36) PRIMARY KEY` | 被审核商品ID，主键 |
| `hs_code` | `hs_code VARCHAR(20) NOT NULL` | 商品海关HS编码 |
| `import_mode` | `import_mode VARCHAR(10) NOT NULL` | 商品进口模式代码 |
| `port_code` | `port_code VARCHAR(40) NOT NULL` | 进口口岸代码 |
| `list_version` | `list_version VARCHAR(80) NOT NULL` | 审核所依据清单的版本标识 |
| `official_source` | `official_source VARCHAR(800) NOT NULL` | 审核所依据的官方信息来源 |
| `decision` | `decision VARCHAR(20) NOT NULL DEFAULT 'pending'` | 商品审核结论，默认pending待审核 |
| `reviewed_by` | `reviewed_by CHAR(36)` | 审核人员用户ID |
| `reviewed_at` | `reviewed_at DATETIME` | 审核时间 |
| `review_note` | `review_note VARCHAR(1000)` | 审核说明 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 审核记录创建时间 |

## `t_return_cases` — 跨境退货案件、申报及货物到达时限记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 退货案件UUID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `applicant_id` | `applicant_id CHAR(36) NOT NULL` | 退货申请用户ID |
| `reason` | `reason VARCHAR(1000) NOT NULL` | 退货申请原因 |
| `status` | `status VARCHAR(30) NOT NULL DEFAULT 'requested'` | 退货处理状态，默认requested已申请 |
| `customs_application_at` | `customs_application_at DATETIME` | 实际向海关提交退货申请时间 |
| `goods_arrived_at` | `goods_arrived_at DATETIME` | 退货货物实际到达时间 |
| `customs_application_deadline` | `customs_application_deadline DATETIME NOT NULL` | 海关退货申请截止时间 |
| `goods_arrival_deadline` | `goods_arrival_deadline DATETIME NOT NULL` | 退货货物到达截止时间 |
| `tax_benefit_eligible` | `tax_benefit_eligible BOOLEAN NOT NULL DEFAULT TRUE` | 是否符合税收优惠条件：1是、0否 |
| `customs_reference` | `customs_reference VARCHAR(100)` | 海关退货业务参考编号 |
| `destination_facility` | `destination_facility VARCHAR(200)` | 退货目的监管设施名称 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 退货案件创建时间 |
| `updated_at` | `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 退货案件更新时间 |

## `t_after_sales_cases` — 订单售后申请、证据及退款处理记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 售后案件UUID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `applicant_id` | `applicant_id CHAR(36) NOT NULL` | 售后申请用户ID |
| `case_type` | `case_type VARCHAR(32) NOT NULL` | 售后案件类型编码 |
| `reason` | `reason VARCHAR(2000) NOT NULL` | 售后申请原因 |
| `evidence_upload_ids` | `evidence_upload_ids JSON` | 售后证据上传记录ID列表，JSON数组 |
| `status` | `status VARCHAR(24) NOT NULL DEFAULT 'submitted'` | 售后案件处理状态，默认submitted已提交 |
| `merchant_response` | `merchant_response VARCHAR(2000)` | 商家对售后申请的回复 |
| `operator_note` | `operator_note VARCHAR(1000)` | 运营处理备注 |
| `refund_status` | `refund_status VARCHAR(32) NOT NULL DEFAULT 'provider_unavailable'` | 退款执行状态，默认provider_unavailable渠道不可用 |
| `refund_amount` | `refund_amount DECIMAL(14,2)` | 售后退款金额，单位人民币元，保留两位小数 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 售后案件创建时间 |
| `updated_at` | `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 售后案件更新时间 |

## `t_consent_records` — 个人信息处理告知及用户同意选择留痕

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 同意留痕记录UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 作出同意选择的用户ID |
| `consent_type` | `consent_type VARCHAR(40) NOT NULL` | 用户同意事项类型 |
| `notice_version` | `notice_version VARCHAR(80) NOT NULL` | 所展示告知文本版本 |
| `recipient_name` | `recipient_name VARCHAR(200)` | 信息接收方名称 |
| `recipient_contact` | `recipient_contact VARCHAR(300)` | 信息接收方联系方式 |
| `purpose` | `purpose VARCHAR(500)` | 个人信息处理目的 |
| `information_types` | `information_types VARCHAR(1000)` | 涉及的个人信息种类说明 |
| `rights_method` | `rights_method VARCHAR(1000)` | 用户行使权利的方式说明 |
| `accepted` | `accepted BOOLEAN NOT NULL` | 用户是否同意：1同意、0不同意 |
| `accepted_at` | `accepted_at DATETIME NOT NULL` | 用户作出同意选择的时间 |
| `source` | `source VARCHAR(40) NOT NULL` | 同意选择的采集来源 |

## `t_auth_credentials` — 用户密码认证凭据摘要

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `user_id` | `user_id CHAR(36) NOT NULL PRIMARY KEY` | 用户ID，主键 |
| `password_hash` | `password_hash VARCHAR(120) NOT NULL` | 密码单向散列值，不保存明文密码 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 密码凭据最近更新时间 |

## `t_login_codes` — 手机号登录验证码摘要、有效期及校验次数

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `phone` | `phone VARCHAR(32) PRIMARY KEY` | 接收登录验证码的手机号，主键 |
| `code_hash` | `code_hash CHAR(64) NOT NULL` | 验证码摘要，不保存验证码明文 |
| `expires_at` | `expires_at DATETIME NOT NULL` | 登录验证码过期时间 |
| `attempts` | `attempts INT NOT NULL DEFAULT 0` | 验证码校验尝试次数，默认0 |
| `updated_at` | `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 验证码记录更新时间 |

## `t_auth_rate_limits` — 认证操作按主体及时间窗口限流计数

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `scope_name` | `scope_name VARCHAR(32) NOT NULL` | 认证限流场景名称，与主体摘要组成主键 |
| `subject_hash` | `subject_hash CHAR(64) NOT NULL` | 限流主体摘要，与场景名称组成主键；不保存主体原文 |
| `used` | `used INT NOT NULL DEFAULT 0` | 当前限流窗口内已使用次数 |
| `window_end` | `window_end DATETIME NOT NULL` | 当前限流窗口结束时间 |

## `t_sms_send_events` — 短信发送事件的手机号和来源IP摘要记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id BIGINT PRIMARY KEY AUTO_INCREMENT` | 短信发送事件自增主键 |
| `phone_hash` | `phone_hash CHAR(64) NOT NULL` | 接收手机号摘要，不保存完整明文手机号 |
| `ip_hash` | `ip_hash CHAR(64) NOT NULL` | 请求来源IP摘要，不保存明文IP |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 短信发送事件记录时间 |

## `t_sms_daily_budget` — 短信每日发送量计数

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `budget_day` | `budget_day DATE PRIMARY KEY` | 短信发送统计日期，主键 |
| `send_count` | `send_count INT NOT NULL DEFAULT 0` | 当日累计发送次数，默认0 |

## `t_store_applications` — 商家开店资质申请及运营审核记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 开店申请UUID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 提交开店申请的用户ID |
| `business_name` | `business_name VARCHAR(200) NOT NULL` | 经营主体名称 |
| `unified_social_credit_code` | `unified_social_credit_code VARCHAR(32) NOT NULL` | 经营主体统一社会信用代码 |
| `legal_representative` | `legal_representative VARCHAR(100) NOT NULL` | 经营主体法定代表人姓名 |
| `license_upload_id` | `license_upload_id CHAR(36) NOT NULL` | 营业执照上传记录ID |
| `invitation_code` | `invitation_code VARCHAR(16)` | 申请所使用的邀请码 |
| `application_channel` | `application_channel VARCHAR(20) NOT NULL` | 开店申请来源渠道 |
| `status` | `status VARCHAR(20) NOT NULL DEFAULT 'pending'` | 开店申请审核状态，默认pending待审核 |
| `review_note` | `review_note VARCHAR(1000)` | 开店审核备注 |
| `reviewed_by` | `reviewed_by CHAR(36)` | 审核运营用户ID |
| `reviewed_at` | `reviewed_at DATETIME` | 开店审核时间 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 开店申请创建时间 |
| `updated_at` | `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 开店申请更新时间 |

## `t_stores` — 店铺主档及撮合费率配置

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 店铺主键 |
| `owner_id` | `owner_id CHAR(36) NOT NULL UNIQUE` | 店主用户ID |
| `application_id` | `application_id CHAR(36) NOT NULL UNIQUE` | 获批的开店申请ID |
| `name` | `name VARCHAR(200) NOT NULL` | 店铺名称 |
| `description` | `description VARCHAR(1000)` | 店铺简介 |
| `matching_fee_rate` | `matching_fee_rate DECIMAL(5,2) NOT NULL DEFAULT 5.00` | 店铺撮合费率参考配置（百分数）；当前代码固定5%，不额外重复收费 |
| `status` | `status VARCHAR(20) NOT NULL DEFAULT 'active'` | 店铺状态代码，默认active |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 店铺创建时间 |

## `t_store_insurance_policies` — 店铺保险保单及外部核验记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 保单记录主键 |
| `store_id` | `store_id CHAR(36) NOT NULL` | 所属店铺ID |
| `insurer_name` | `insurer_name VARCHAR(200) NOT NULL` | 保险公司名称 |
| `policy_no` | `policy_no VARCHAR(120) NOT NULL` | 保险保单号 |
| `coverage_amount` | `coverage_amount DECIMAL(14,2) NOT NULL` | 保额，单位为currency指定币种的主货币单位 |
| `currency` | `currency CHAR(3) NOT NULL DEFAULT 'CNY'` | 保单币种代码，默认CNY |
| `expires_at` | `expires_at DATE NOT NULL` | 保单到期日期 |
| `certificate_upload_id` | `certificate_upload_id CHAR(36) NOT NULL` | 保单凭证上传记录ID |
| `status` | `status VARCHAR(40) NOT NULL DEFAULT 'pending_provider_verification'` | 保单核验状态代码，默认pending_provider_verification |
| `external_verification_ref` | `external_verification_ref VARCHAR(160)` | 保险机构外部核验流水号 |
| `verification_channel` | `verification_channel VARCHAR(30)` | 保单核验渠道代码 |
| `verified_by` | `verified_by CHAR(36)` | 核验操作人用户ID |
| `verified_at` | `verified_at DATETIME` | 核验完成时间 |
| `review_note` | `review_note VARCHAR(1000)` | 核验审核意见 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 保单记录创建时间 |

## `t_insurance_coverages` — 订单关联的店铺保单承保记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 承保记录主键 |
| `order_id` | `order_id CHAR(36) NOT NULL UNIQUE` | 被承保订单ID，每个订单唯一 |
| `policy_id` | `policy_id CHAR(36) NOT NULL` | 关联店铺保险保单ID |
| `external_coverage_ref` | `external_coverage_ref VARCHAR(160) NOT NULL` | 保险机构承保流水号 |
| `declared_value` | `declared_value DECIMAL(14,2) NOT NULL` | 货物申报价值，单位为关联保单币种的主货币单位 |
| `premium_amount` | `premium_amount DECIMAL(14,2) NOT NULL` | 保险机构直接收取的保费金额（关联保单币种主货币单位），不进入平台托管 |
| `premium_paid_directly` | `premium_paid_directly BOOLEAN NOT NULL` | 是否直接支付保险费，1是、0否 |
| `status` | `status VARCHAR(24) NOT NULL` | 承保状态代码 |
| `created_at` | `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` | 承保记录创建时间 |

## `t_risk_pool` — 历史风险准备金余额记录；不代表平台承保或担保

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id TINYINT PRIMARY KEY` | 资金池记录主键 |
| `balance` | `balance DECIMAL(14,2) NOT NULL DEFAULT 0` | 历史资金池余额，保留两位小数；本表无独立币种，不作为平台承保或赔付承诺 |

## `t_integration_vendors` — 第三方集成服务商目录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `code` | `code VARCHAR(32) NOT NULL PRIMARY KEY` | 服务商代码，作为主键 |
| `name` | `name VARCHAR(80) NOT NULL` | 服务商显示名称 |
| `sort_no` | `sort_no INT NOT NULL DEFAULT 0` | 目录排序值 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 服务商记录创建时间 |

## `t_integration_products` — 第三方集成产品目录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `vendor` | `vendor VARCHAR(32) NOT NULL` | 所属服务商代码 |
| `code` | `code VARCHAR(64) NOT NULL` | 集成产品代码，与服务商共同标识产品 |
| `name` | `name VARCHAR(80) NOT NULL` | 集成产品显示名称 |
| `category` | `category VARCHAR(32) NOT NULL` | 产品所属功能分类代码 |
| `sort_no` | `sort_no INT NOT NULL DEFAULT 0` | 产品目录排序值 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 产品记录创建时间 |

## `t_integration_product_fields` — 集成产品配置字段定义

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `vendor` | `vendor VARCHAR(32) NOT NULL` | 所属服务商代码 |
| `product` | `product VARCHAR(64) NOT NULL` | 所属集成产品代码 |
| `field_key` | `field_key VARCHAR(64) NOT NULL` | 配置字段键名 |
| `label` | `label VARCHAR(80) NOT NULL` | 配置字段显示标签 |
| `secret` | `secret TINYINT NOT NULL DEFAULT 0` | 是否为敏感字段，1是、0否；敏感明文不向管理目录返回 |
| `sort_no` | `sort_no INT NOT NULL DEFAULT 0` | 配置字段显示排序值 |

## `t_integration_settings` — 集成产品启用状态及管理备注

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `vendor` | `vendor VARCHAR(32) NOT NULL` | 所属服务商代码 |
| `product` | `product VARCHAR(64) NOT NULL` | 所属集成产品代码 |
| `enabled` | `enabled TINYINT NOT NULL DEFAULT 0` | 是否启用该集成产品，1是、0否 |
| `remark` | `remark VARCHAR(200) NULL` | 集成配置管理备注 |
| `updated_by` | `updated_by VARCHAR(36) NULL` | 最后修改人用户ID |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 配置最后更新时间 |

## `t_integration_secrets` — 集成产品配置值密文存储

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `vendor` | `vendor VARCHAR(32) NOT NULL` | 所属服务商代码 |
| `product` | `product VARCHAR(64) NOT NULL` | 所属集成产品代码 |
| `field_key` | `field_key VARCHAR(64) NOT NULL` | 配置字段键名 |
| `field_cipher` | `field_cipher TEXT NOT NULL` | 配置值密文；按服务商、产品和字段作用域加密，仅运行配置解析时解密，禁止作为明文展示 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 配置值最后更新时间 |

## `t_posts` — 社区帖子内容及互动计数

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 帖子主键 |
| `author_id` | `author_id VARCHAR(36) NOT NULL` | 发帖人用户ID |
| `kind` | `kind VARCHAR(16) NOT NULL` | 帖子类型代码 |
| `title` | `title VARCHAR(200) NULL` | 帖子标题 |
| `body` | `body VARCHAR(4000) NULL` | 帖子正文 |
| `image_urls` | `image_urls JSON NULL` | 帖子图片URL列表，JSON数组 |
| `order_ref` | `order_ref VARCHAR(36) NULL` | 帖子关联订单ID |
| `like_count` | `like_count INT NOT NULL DEFAULT 0` | 帖子点赞数量 |
| `comment_count` | `comment_count INT NOT NULL DEFAULT 0` | 帖子评论数量 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 帖子审核或展示状态代码，默认pending |
| `removed_reason` | `removed_reason VARCHAR(200) NULL` | 帖子移除原因 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 帖子发布时间 |

## `t_post_likes` — 社区帖子点赞关系

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `post_id` | `post_id VARCHAR(36) NOT NULL` | 被点赞帖子ID |
| `user_id` | `user_id VARCHAR(36) NOT NULL` | 点赞用户ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 点赞时间 |

## `t_post_comments` — 社区帖子评论内容

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 评论主键 |
| `post_id` | `post_id VARCHAR(36) NOT NULL` | 所属帖子ID |
| `author_id` | `author_id VARCHAR(36) NOT NULL` | 评论作者用户ID |
| `content` | `content VARCHAR(500) NOT NULL` | 评论文本内容 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 评论发布时间 |

## `t_community_uploads` — 社区上传文件元数据及使用关联

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 上传记录主键 |
| `user_id` | `user_id VARCHAR(36) NOT NULL` | 上传者用户ID |
| `kind` | `kind VARCHAR(16) NOT NULL` | 上传媒体类型代码 |
| `storage_key` | `storage_key VARCHAR(400) NOT NULL` | 文件在存储服务中的对象键 |
| `content_type` | `content_type VARCHAR(80) NULL` | 文件媒体类型（MIME） |
| `purpose` | `purpose VARCHAR(40) NULL` | 上传用途代码 |
| `order_id` | `order_id CHAR(36) NULL` | 关联订单ID |
| `trip_id` | `trip_id CHAR(36) NULL` | 关联行程ID |
| `sha256` | `sha256 CHAR(64) NULL` | 文件内容SHA-256摘要，十六进制字符串 |
| `byte_size` | `byte_size BIGINT NULL` | 文件大小，单位字节 |
| `original_name` | `original_name VARCHAR(200) NULL` | 上传文件原始名称 |
| `consumed_at` | `consumed_at TIMESTAMP(3) NULL` | 上传文件被业务消费的时间 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 上传记录创建时间 |

## `t_post_media` — 社区帖子的媒体及封面关联

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `post_id` | `post_id VARCHAR(36) NOT NULL PRIMARY KEY` | 帖子ID，每个帖子对应一条媒体记录 |
| `media_kind` | `media_kind VARCHAR(16) NOT NULL` | 媒体类型代码 |
| `media_url` | `media_url VARCHAR(800) NULL` | 媒体访问URL |
| `poster_url` | `poster_url VARCHAR(800) NULL` | 媒体封面访问URL |
| `upload_id` | `upload_id VARCHAR(36) NULL` | 媒体文件上传记录ID |
| `poster_upload_id` | `poster_upload_id VARCHAR(36) NULL` | 封面文件上传记录ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 媒体关联创建时间 |

## `t_post_anchors` — 社区帖子挂载的关联资源及标题快照

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `post_id` | `post_id VARCHAR(36) NOT NULL PRIMARY KEY` | 帖子ID，每个帖子对应一条挂载记录 |
| `anchor_type` | `anchor_type VARCHAR(16) NOT NULL` | 挂载资源类型代码 |
| `anchor_id` | `anchor_id VARCHAR(36) NULL` | 挂载资源ID |
| `title_snapshot` | `title_snapshot VARCHAR(200) NULL` | 挂载时的资源标题快照 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 挂载记录创建时间 |

## `t_post_favorites` — 用户收藏社区帖子的关系

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `post_id` | `post_id VARCHAR(36) NOT NULL` | 被收藏帖子ID |
| `user_id` | `user_id VARCHAR(36) NOT NULL` | 收藏用户ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 收藏时间 |

## `t_post_shares` — 社区帖子分享行为记录

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 分享记录主键 |
| `post_id` | `post_id VARCHAR(36) NOT NULL` | 被分享帖子ID |
| `user_id` | `user_id VARCHAR(36) NOT NULL` | 分享用户ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 分享发生时间 |

## `t_geo_points` — 业务资源关联的地理坐标点

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 坐标记录主键 |
| `ref_type` | `ref_type VARCHAR(16) NOT NULL` | 关联资源类型代码 |
| `ref_id` | `ref_id VARCHAR(36) NOT NULL` | 关联资源ID |
| `kind` | `kind VARCHAR(24) NOT NULL` | 坐标点用途类型代码 |
| `lat` | `lat DECIMAL(10,7) NOT NULL` | 纬度，单位度 |
| `lng` | `lng DECIMAL(10,7) NOT NULL` | 经度，单位度 |
| `place_name` | `place_name VARCHAR(120) NULL` | 地点名称 |
| `address` | `address VARCHAR(240) NULL` | 地点详细地址 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 坐标记录创建时间 |

## `t_procurement_venues` — 采购场所目录及地理信息

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 采购场所主键 |
| `name` | `name VARCHAR(120) NOT NULL` | 采购场所名称 |
| `city` | `city VARCHAR(64) NOT NULL` | 采购场所所在城市 |
| `lat` | `lat DECIMAL(10,7) NOT NULL` | 场所纬度，单位度 |
| `lng` | `lng DECIMAL(10,7) NOT NULL` | 场所经度，单位度 |
| `address` | `address VARCHAR(240) NULL` | 场所详细地址 |
| `partner_flag` | `partner_flag TINYINT NOT NULL DEFAULT 0` | 是否为合作场所，1是、0否 |
| `keywords` | `keywords VARCHAR(240) NULL` | 场所检索关键词文本 |
| `active` | `active TINYINT NOT NULL DEFAULT 1` | 场所是否启用，1是、0否 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 场所记录创建时间 |

## `t_direct_threads` — 用户间私信会话及来源帖子

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 私信会话主键 |
| `user_low` | `user_low VARCHAR(36) NOT NULL` | 会话双方中排序较小的用户ID |
| `user_high` | `user_high VARCHAR(36) NOT NULL` | 会话双方中排序较大的用户ID |
| `post_id` | `post_id VARCHAR(36) NULL` | 引发会话的来源帖子ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 会话创建时间 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 会话最后更新时间 |

## `t_direct_messages` — 私信会话消息内容

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 私信消息主键 |
| `thread_id` | `thread_id VARCHAR(36) NOT NULL` | 所属私信会话ID |
| `sender_id` | `sender_id VARCHAR(36) NOT NULL` | 消息发送人用户ID |
| `content` | `content VARCHAR(500) NOT NULL` | 私信消息文本 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 消息发送时间 |

## `t_piggyback_requests` — 针对行程发起的捎带请求

来源：原有表；`server/db/reference/current-schema.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(36) NOT NULL PRIMARY KEY` | 捎带请求主键 |
| `trip_id` | `trip_id VARCHAR(36) NOT NULL` | 请求关联行程ID |
| `post_id` | `post_id VARCHAR(36) NULL` | 请求来源帖子ID |
| `asker_id` | `asker_id VARCHAR(36) NOT NULL` | 提出捎带请求的用户ID |
| `demand_id` | `demand_id VARCHAR(36) NULL` | 请求关联需求ID |
| `title` | `title VARCHAR(200) NOT NULL` | 捎带请求标题 |
| `note` | `note VARCHAR(4000) NULL` | 捎带请求补充说明 |
| `city` | `city VARCHAR(64) NULL` | 请求涉及城市 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 捎带请求处理状态代码，默认pending |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 捎带请求创建时间 |

## `t_withdrawals` — 用户提现申请及后台审核记录

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 提现申请主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 申请提现的用户ID |
| `amount` | `amount DECIMAL(12,2) NOT NULL` | 申请提现金额，单位元，保留两位小数 |
| `channel` | `channel VARCHAR(32) NOT NULL DEFAULT 'manual'` | 提现渠道代码，默认manual |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 提现申请处理状态代码，默认pending |
| `admin_note` | `admin_note VARCHAR(2000) NULL` | 提现审核管理备注 |
| `reviewed_by` | `reviewed_by CHAR(36) NULL` | 审核人用户ID |
| `reviewed_at` | `reviewed_at DATETIME(3) NULL` | 提现审核时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 提现申请创建时间 |

## `t_announcements` — 平台公告及多语言内容

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 公告主键 |
| `title` | `title VARCHAR(255) NOT NULL` | 公告默认语言标题 |
| `body` | `body TEXT NOT NULL` | 公告默认语言正文 |
| `kind` | `kind VARCHAR(32) NOT NULL DEFAULT 'system'` | 公告类型代码，默认system |
| `pinned` | `pinned TINYINT(1) NOT NULL DEFAULT 0` | 是否置顶，1是、0否 |
| `active` | `active TINYINT(1) NOT NULL DEFAULT 1` | 公告是否启用，1是、0否 |
| `created_by` | `created_by CHAR(36) NOT NULL` | 公告创建人用户ID |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 公告创建时间 |
| `title_i18n` | `title_i18n JSON NULL` | 多语言公告标题，JSON对象，按语言代码存储 |
| `body_i18n` | `body_i18n JSON NULL` | 多语言公告正文，JSON对象，按语言代码存储 |

## `t_proof_recipes` — 按行程及交通方式配置的凭证材料规则

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 凭证规则主键 |
| `trip_type` | `trip_type VARCHAR(32) NOT NULL` | 适用行程类型代码 |
| `transport_mode` | `transport_mode VARCHAR(32) NOT NULL` | 适用交通方式代码 |
| `country_code` | `country_code VARCHAR(8) NULL` | 适用国家或地区代码，空值表示未指定 |
| `items` | `items JSON NOT NULL` | 规则要求的凭证材料项目，JSON结构 |
| `note` | `note VARCHAR(2000) NULL` | 凭证规则补充说明 |
| `active` | `active TINYINT(1) NOT NULL DEFAULT 1` | 规则是否启用，1是、0否 |
| `sort_order` | `sort_order INT NOT NULL DEFAULT 100` | 规则排序值 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 凭证规则创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 凭证规则最后更新时间 |

## `t_channel_configs` — 外部渠道供应商配置，保存渠道参数与启用状态

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 渠道配置主键 |
| `channel_type` | `channel_type VARCHAR(32) NOT NULL` | 渠道类型：sms短信、email邮件、wx_subscribe微信订阅、privacy_number隐私号、logistics物流、payment支付、ai人工智能、rtc实时通信 |
| `provider` | `provider VARCHAR(64) NOT NULL` | 渠道供应商标识 |
| `config` | `config JSON NOT NULL` | 渠道参数JSON对象；可能包含凭据，管理端按敏感键名脱敏，不代表已加密 |
| `enabled` | `enabled TINYINT(1) NOT NULL DEFAULT 0` | 是否启用该供应商配置，1启用、0停用 |
| `remark` | `remark VARCHAR(2000) NULL` | 配置说明 |
| `updated_by` | `updated_by CHAR(36) NULL` | 最后更新配置的管理员用户ID |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_channel_secrets_probe` — 渠道密钥就绪状态的人工探测记录

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `channel` | `channel VARCHAR(32) NOT NULL PRIMARY KEY` | 渠道标识：wechat_subscribe、sms、email或ai_call |
| `ready` | `ready TINYINT(1) NOT NULL DEFAULT 0` | 渠道密钥是否标记就绪，1就绪、0未就绪；不代表真实调用成功 |
| `probed_at` | `probed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 最近记录探测状态的时间 |

## `t_error_logs` — 客户端及服务异常日志与处理记录

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY` | 自增异常日志主键 |
| `scope` | `scope VARCHAR(128) NOT NULL` | 异常来源或作用域 |
| `message` | `message TEXT NOT NULL` | 异常消息正文 |
| `stack` | `stack MEDIUMTEXT NULL` | 异常堆栈文本，可能含运行上下文，避免泄露敏感数据 |
| `page` | `page VARCHAR(1024) NULL` | 发生异常的页面路径或地址 |
| `user_id` | `user_id CHAR(36) NULL` | 关联用户ID |
| `extra` | `extra JSON NULL` | 异常补充上下文JSON，避免记录凭据及敏感原文 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'open'` | 处理状态：open待处理、resolved已处理 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 异常记录时间 |
| `resolved_at` | `resolved_at DATETIME(3) NULL` | 标记处理完成的时间 |

## `t_assist_requests` — 订单协带申请、两次交接凭证与分润结算

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 协带申请主键 |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `requester_id` | `requester_id CHAR(36) NOT NULL` | 发起协带的原带单人用户ID |
| `assistant_id` | `assistant_id CHAR(36) NULL` | 承接协带的用户ID |
| `share_rate` | `share_rate DECIMAL(5,2) NOT NULL` | 协带分润百分比，按货款与运费之和计算，代码允许20至60 |
| `note` | `note VARCHAR(2000) NULL` | 协带需求说明 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'pending'` | 协带状态：pending待承接、accepted已承接、carrying携带中、done已完成、cancelled已取消 |
| `handover_a_urls` | `handover_a_urls JSON NOT NULL` | 原带单人向协带人交接的照片地址JSON数组 |
| `handover_a_city` | `handover_a_city VARCHAR(64) NULL` | 首次交接所在城市 |
| `handover_a_at` | `handover_a_at DATETIME(3) NULL` | 首次交接凭证提交时间 |
| `handover_b_urls` | `handover_b_urls JSON NOT NULL` | 协带人向买家交付的照片地址JSON数组 |
| `handover_b_city` | `handover_b_city VARCHAR(64) NULL` | 最终交付所在城市 |
| `finished_at` | `finished_at DATETIME(3) NULL` | 协带完成并结算的时间 |
| `settled_amount` | `settled_amount DECIMAL(12,2) NULL` | 实际结算分润金额，单位人民币元 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 协带申请创建时间 |

## `t_enterprise_license_files` — 企业认证营业执照附件及排列顺序

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 营业执照附件主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 提交营业执照的用户ID |
| `storage_url` | `storage_url VARCHAR(1024) NOT NULL` | 营业执照文件存储地址，属认证资料，访问应受控 |
| `position_no` | `position_no INT NOT NULL DEFAULT 0` | 附件显示顺序编号 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 附件记录创建时间 |

## `t_locales` — 可用界面语言、显示名称与回退语言配置

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `code` | `code VARCHAR(16) NOT NULL PRIMARY KEY` | 语言或地区代码，作为主键 |
| `native_name` | `native_name VARCHAR(64) NOT NULL` | 该语言的本地名称 |
| `label_zh` | `label_zh VARCHAR(64) NOT NULL` | 语言的中文显示名称 |
| `sort_order` | `sort_order INT NOT NULL DEFAULT 0` | 语言选项排序值 |
| `active` | `active TINYINT(1) NOT NULL DEFAULT 1` | 是否启用该语言，1启用、0停用 |
| `fallback_code` | `fallback_code VARCHAR(16) NULL` | 翻译缺失时使用的回退语言代码 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 语言配置创建时间 |

## `t_logistics_tracking` — 订单物流单号及最近查询的轨迹缓存

来源：提议新增表；`server/db/migrations/001-missing-business-tables.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `order_id` | `order_id CHAR(36) NOT NULL PRIMARY KEY` | 关联订单ID，每单一条物流缓存 |
| `provider` | `provider VARCHAR(64) NULL` | 物流服务商或承运商标识 |
| `tracking_no` | `tracking_no VARCHAR(64) NULL` | 物流运单号 |
| `latest_state` | `latest_state VARCHAR(64) NULL` | 最近一次查询返回的物流状态文本，含义由服务商定义 |
| `events_json` | `events_json JSON NULL` | 物流轨迹事件JSON，结构随查询适配器返回内容确定 |
| `refreshed_at` | `refreshed_at DATETIME(3) NULL` | 最近刷新物流信息的时间 |

## `t_payment_merchants` — 多币种支付商户、支付能力与报价参数配置

来源：提议新增表；`server/db/migrations/002-configurable-payments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 支付商户配置主键 |
| `name` | `name VARCHAR(80) NOT NULL` | 商户显示名称 |
| `adapter` | `adapter VARCHAR(40) NOT NULL` | 支付渠道适配器标识 |
| `method` | `method VARCHAR(24) NOT NULL` | 支付方式标识 |
| `platform` | `platform VARCHAR(24) NOT NULL` | 适用客户端平台标识 |
| `currencies` | `currencies JSON NOT NULL` | 支持支付币种代码的JSON集合 |
| `credential_prefix` | `credential_prefix VARCHAR(40) NULL` | 支付凭据配置引用前缀，不是密钥明文 |
| `spread_bps` | `spread_bps INT NOT NULL DEFAULT 0` | 汇率加点，单位基点，1基点为0.01% |
| `quote_ttl_seconds` | `quote_ttl_seconds INT NOT NULL DEFAULT 300` | 报价有效期上限，单位秒 |
| `enabled` | `enabled BOOLEAN NOT NULL DEFAULT FALSE` | 是否启用该商户，1启用、0停用 |
| `revision` | `revision BIGINT NOT NULL DEFAULT 1` | 商户配置版本号，用于检测报价后配置变化 |
| `settings` | `settings JSON NOT NULL` | 支付适配器设置JSON，含通知地址、返回地址、网关及收银台域名等配置 |
| `updated_by` | `updated_by CHAR(36) NOT NULL` | 最后更新配置的管理员用户ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_fx_sources` — 外汇汇率数据源及交易使用开关配置

来源：提议新增表；`server/db/migrations/002-configurable-payments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 汇率源配置主键 |
| `name` | `name VARCHAR(80) NOT NULL` | 汇率源显示名称 |
| `kind` | `kind VARCHAR(32) NOT NULL` | 汇率源适配类型：open_er_api或rates_json_v1 |
| `endpoint` | `endpoint VARCHAR(1000) NULL` | 汇率数据接口地址 |
| `credential_prefix` | `credential_prefix VARCHAR(40) NULL` | 访问凭据配置引用前缀，不是密钥明文 |
| `max_age_seconds` | `max_age_seconds INT NOT NULL DEFAULT 86400` | 可接受汇率数据的最长年龄，单位秒 |
| `enabled` | `enabled BOOLEAN NOT NULL DEFAULT FALSE` | 是否启用汇率源，1启用、0停用 |
| `transaction_enabled` | `transaction_enabled BOOLEAN NOT NULL DEFAULT FALSE` | 是否允许该汇率源用于实际交易报价，1允许、0禁止 |
| `updated_by` | `updated_by CHAR(36) NOT NULL` | 最后更新配置的管理员用户ID |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_payment_quotes` — 锁定支付商户配置和汇率的限时支付报价

来源：提议新增表；`server/db/migrations/002-configurable-payments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 支付报价主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 申请报价的用户ID |
| `merchant_id` | `merchant_id CHAR(36) NOT NULL` | 支付商户配置ID |
| `merchant_revision` | `merchant_revision BIGINT NOT NULL` | 报价时商户配置版本号 |
| `merchant_snapshot` | `merchant_snapshot JSON NOT NULL` | 报价时商户配置JSON快照，含配置引用，按敏感支付配置受控访问 |
| `base_currency` | `base_currency CHAR(3) NOT NULL DEFAULT 'CNY'` | 基准金额币种代码，默认CNY |
| `base_minor` | `base_minor DECIMAL(24,0) NOT NULL` | 基准金额的最小货币单位整数，CNY时单位分 |
| `payment_currency` | `payment_currency CHAR(3) NOT NULL` | 实际支付币种代码 |
| `payment_minor` | `payment_minor DECIMAL(24,0) NOT NULL` | 支付金额的最小货币单位整数，小数位由payment_minor_digits定义 |
| `payment_minor_digits` | `payment_minor_digits INT NOT NULL` | 支付币种主单位的小数位数 |
| `fx_rate` | `fx_rate DECIMAL(36,18) NOT NULL` | 从基准币种换算为支付币种的汇率 |
| `spread_bps` | `spread_bps INT NOT NULL` | 本次报价汇率加点，单位基点，1基点为0.01% |
| `rate_source` | `rate_source VARCHAR(80) NOT NULL` | 本次报价使用的汇率来源标识 |
| `rate_as_of` | `rate_as_of TIMESTAMP(3) NOT NULL` | 汇率数据对应时间 |
| `expires_at` | `expires_at TIMESTAMP(3) NOT NULL` | 报价失效时间 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 报价创建时间 |

## `t_currency_payments` — 多币种充值支付尝试、渠道收银台信息与核验状态

来源：提议新增表；`server/db/migrations/002-configurable-payments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(32) PRIMARY KEY` | 支付单主键，32位标识 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 充值用户ID |
| `request_id` | `request_id CHAR(36) NOT NULL` | 用户请求幂等标识 |
| `quote_id` | `quote_id CHAR(36) NOT NULL` | 锁定的支付报价ID |
| `merchant_id` | `merchant_id CHAR(36) NOT NULL` | 支付商户配置ID |
| `merchant_snapshot` | `merchant_snapshot JSON NOT NULL` | 支付时商户配置JSON快照，用于后续查询核验，敏感配置受控访问 |
| `base_currency` | `base_currency CHAR(3) NOT NULL DEFAULT 'CNY'` | 基准充值币种代码，默认CNY |
| `base_minor` | `base_minor DECIMAL(24,0) NOT NULL` | 基准充值金额的最小货币单位整数，CNY时单位分 |
| `payment_currency` | `payment_currency CHAR(3) NOT NULL` | 支付币种代码 |
| `payment_minor` | `payment_minor DECIMAL(24,0) NOT NULL` | 支付金额的最小货币单位整数，小数位由payment_minor_digits定义 |
| `payment_minor_digits` | `payment_minor_digits INT NOT NULL` | 支付币种主单位的小数位数 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'creating'` | 支付状态：creating创建中、pending待支付、paid已核验到账 |
| `checkout_kind` | `checkout_kind VARCHAR(24) NULL` | 收银台类型：dispatching请求已占用，或redirect、qr_text、wechat_jsapi、wechat_app、alipay_app |
| `checkout_value` | `checkout_value TEXT NULL` | 收银台跳转地址、二维码文本或客户端支付参数，可能含临时支付数据，访问应受控 |
| `provider_transaction_id` | `provider_transaction_id VARCHAR(128) NULL` | 渠道交易流水号 |
| `last_checked_at` | `last_checked_at TIMESTAMP(3) NULL` | 最近核查时间 |
| `last_error_code` | `last_error_code VARCHAR(64) NULL` | 最近一次支付查询的安全错误码 |
| `paid_at` | `paid_at TIMESTAMP(3) NULL` | 支付成功核验入账时间 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_currency_payment_receipts` — 已核验支付回执，用于防止重复充值入账

来源：提议新增表；`server/db/migrations/002-configurable-payments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 回执主键 |
| `payment_id` | `payment_id CHAR(32) NOT NULL` | 关联支付单ID |
| `merchant_id` | `merchant_id CHAR(36) NOT NULL` | 支付商户配置ID |
| `provider_transaction_id` | `provider_transaction_id VARCHAR(128) NOT NULL` | 已核验的渠道交易流水号 |
| `payment_currency` | `payment_currency CHAR(3) NOT NULL` | 回执支付币种代码 |
| `payment_minor` | `payment_minor DECIMAL(24,0) NOT NULL` | 已核验支付金额的最小货币单位整数 |
| `payment_minor_digits` | `payment_minor_digits INT NOT NULL` | 支付币种主单位的小数位数 |
| `base_minor` | `base_minor DECIMAL(24,0) NOT NULL` | 对应基准充值金额的最小货币单位整数，人民币以分计 |
| `verified_at` | `verified_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 渠道回执核验并持久化时间 |

## `t_goods_insurance_products` — 货物保险产品及保险方接入参数配置

来源：提议新增表；`server/db/migrations/003-goods-insurance.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id VARCHAR(64) PRIMARY KEY` | 保险产品标识 |
| `insurer_code` | `insurer_code VARCHAR(64) NOT NULL` | 承保保险机构代码 |
| `config_json` | `config_json JSON NOT NULL` | 保险产品配置JSON，含适配器、产品代码、凭据引用前缀、条款及服务域名等；不应存放密钥明文 |
| `enabled` | `enabled BOOLEAN NOT NULL DEFAULT FALSE` | 产品是否启用，1启用、0停用 |
| `revision` | `revision BIGINT NOT NULL DEFAULT 1` | 保险产品配置版本号 |
| `updated_by` | `updated_by CHAR(36) NOT NULL` | 最后更新产品的管理员用户ID |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_goods_insurance_quotes` — 保险方返回的货物保险限时报价及产品快照

来源：提议新增表；`server/db/migrations/003-goods-insurance.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 保险报价主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 申请报价的用户ID |
| `subject_type` | `subject_type VARCHAR(16) NOT NULL` | 报价标的类型：trip行程、pk_quote竞价报价、order订单 |
| `platform` | `platform VARCHAR(16) NOT NULL` | 发起报价的客户端平台：web、h5、app或mp_weixin |
| `subject_id` | `subject_id CHAR(36) NOT NULL` | 对应行程、竞价报价或订单ID |
| `item_name` | `item_name VARCHAR(255) NOT NULL` | 投保货物名称 |
| `product_id` | `product_id VARCHAR(64) NOT NULL` | 保险产品标识 |
| `product_revision` | `product_revision BIGINT NOT NULL` | 报价时保险产品配置版本号 |
| `declared_value` | `declared_value DECIMAL(14,2) NOT NULL` | 货物申报价值，按输入货物价格主单位记录；当前订单货款为人民币元 |
| `premium` | `premium DECIMAL(24,6) NOT NULL` | 保险方报价保费，单位为currency对应币种的主单位 |
| `currency` | `currency CHAR(3) NOT NULL` | 保险保费币种代码 |
| `provider_ref` | `provider_ref VARCHAR(160) NOT NULL` | 保险方返回的报价引用标识 |
| `terms_version` | `terms_version VARCHAR(160) NOT NULL` | 保险方返回的条款版本标识 |
| `snapshot_json` | `snapshot_json JSON NOT NULL` | 报价时保险产品配置JSON快照，含凭据引用，访问应受控 |
| `expires_at` | `expires_at DATETIME(3) NOT NULL` | 报价失效时间 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 报价创建时间 |

## `t_goods_insurance_applications` — 订单保险投保申请及承保方核验结果

来源：提议新增表；`server/db/migrations/003-goods-insurance.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 投保申请主键 |
| `user_id` | `user_id CHAR(36) NOT NULL` | 投保用户ID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `quote_id` | `quote_id CHAR(36) NOT NULL` | 采用的保险报价ID |
| `insurer_code` | `insurer_code VARCHAR(64) NOT NULL` | 保险机构代码 |
| `request_id` | `request_id CHAR(36) NOT NULL` | 用户投保请求幂等标识 |
| `status` | `status VARCHAR(32) NOT NULL DEFAULT 'reserved'` | 投保状态：reserved已预留、dispatching提交中、awaiting_provider待保险方确认、scheduled待生效、active已生效、rejected拒保、cancelled已取消、expired已过期 |
| `provider_application_ref` | `provider_application_ref VARCHAR(160) NULL` | 保险方申请引用标识 |
| `checkout_url` | `checkout_url VARCHAR(1500) NULL` | 保险方收银台地址，可能包含临时访问参数 |
| `policy_number` | `policy_number VARCHAR(160) NULL` | 保险方核验返回的保单号 |
| `coverage_ref` | `coverage_ref VARCHAR(160) NULL` | 保险保障记录引用标识 |
| `certificate_url` | `certificate_url VARCHAR(1500) NULL` | 保险凭证地址，含投保资料，访问应受控 |
| `coverage_starts_at` | `coverage_starts_at DATETIME(3) NULL` | 保障起始时间 |
| `coverage_ends_at` | `coverage_ends_at DATETIME(3) NULL` | 保障结束时间 |
| `terms_accepted_at` | `terms_accepted_at DATETIME(3) NOT NULL` | 用户确认保险条款的时间 |
| `disclosures_accepted_at` | `disclosures_accepted_at DATETIME(3) NOT NULL` | 用户确认告知事项的时间 |
| `last_checked_at` | `last_checked_at DATETIME(3) NULL` | 最近向保险方核查的时间 |
| `last_error_code` | `last_error_code VARCHAR(64) NULL` | 最近核查的安全错误码 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 申请创建时间 |

## `t_dispute_events` — 订单争议处理操作及阶段变更审计事件

来源：提议新增表；`server/db/migrations/004-dispute-events.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 争议事件主键 |
| `dispute_id` | `dispute_id CHAR(36) NOT NULL` | 关联争议案件ID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `actor_id` | `actor_id CHAR(36) NOT NULL` | 执行操作的用户ID |
| `actor_role` | `actor_role VARCHAR(16) NOT NULL` | 操作者在争议中的角色标识 |
| `action` | `action VARCHAR(32) NOT NULL` | 争议操作标识 |
| `request_id` | `request_id CHAR(36) NOT NULL` | 操作请求幂等标识 |
| `before_stage` | `before_stage VARCHAR(24) NULL` | 操作前的争议阶段，首次事件可为空 |
| `after_stage` | `after_stage VARCHAR(24) NOT NULL` | 操作后的争议阶段 |
| `payload` | `payload JSON NOT NULL` | 事件输入与结果等JSON载荷，可能含争议证据及请求摘要，访问应受控 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 事件创建时间 |

## `t_store_products` — 店铺与商品的归属关联

来源：提议新增表；`server/db/migrations/005-store-products.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `store_id` | `store_id CHAR(36) NOT NULL` | 店铺ID |
| `product_id` | `product_id CHAR(36) NOT NULL` | 商品ID |
| `created_by` | `created_by CHAR(36) NOT NULL` | 建立店铺商品关联的用户ID |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 关联创建时间 |

## `t_product_order_reservations` — 商品下单时的库存占用、计价与归属快照

来源：提议新增表；`server/db/migrations/006-commerce-checkout.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `order_id` | `order_id CHAR(36) NOT NULL PRIMARY KEY` | 关联订单ID，作为预留记录主键 |
| `product_id` | `product_id CHAR(36) NOT NULL` | 下单商品ID |
| `buyer_id` | `buyer_id CHAR(36) NOT NULL` | 买家用户ID |
| `vendor_id` | `vendor_id CHAR(36) NOT NULL` | 商品卖家及带单人用户ID |
| `trip_id` | `trip_id CHAR(36) NOT NULL` | 用于履约的行程ID |
| `store_id` | `store_id CHAR(36) NULL` | 下单时商品归属店铺ID |
| `quantity` | `quantity INT NOT NULL` | 下单商品数量 |
| `unit_price` | `unit_price DECIMAL(12,2) NOT NULL` | 商品成交单价，单位为currency对应币种的主单位 |
| `goods_amount` | `goods_amount DECIMAL(12,2) NOT NULL` | 商品货款总额，单位为currency对应币种的主单位 |
| `service_fee` | `service_fee DECIMAL(12,2) NOT NULL` | 订单服务费，单位为currency对应币种的主单位 |
| `currency` | `currency CHAR(3) NOT NULL DEFAULT 'CNY'` | 金额币种代码，当前下单写入CNY |
| `stock_state` | `stock_state VARCHAR(16) NOT NULL DEFAULT 'held'` | 库存占用状态：held已预留、consumed已消耗、released已释放 |
| `request_hash` | `request_hash CHAR(64) NOT NULL` | 下单请求内容哈希，用于核对幂等请求一致性 |
| `snapshot` | `snapshot JSON NOT NULL` | 下单时商品与订单约定JSON快照，用于核对计价和归属 |
| `created_at` | `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_payment_refunds` — 充值原路退款申请、审核、渠道派发与钱包冻结释放记录

来源：提议新增表；`server/db/migrations/007-refunds.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(32) PRIMARY KEY` | 退款单主键，32位标识 |
| `payment_id` | `payment_id CHAR(32) NOT NULL` | 原充值支付单ID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 申请退款的用户ID |
| `request_id` | `request_id CHAR(36) NOT NULL` | 用户退款请求幂等标识 |
| `base_currency` | `base_currency CHAR(3) NOT NULL` | 退款基准币种代码，当前退款仅支持CNY |
| `base_minor` | `base_minor DECIMAL(24,0) NOT NULL` | 基准退款金额的最小货币单位整数，CNY时单位分 |
| `payment_currency` | `payment_currency CHAR(3) NOT NULL` | 原渠道支付及退款币种代码 |
| `payment_minor` | `payment_minor DECIMAL(24,0) NOT NULL` | 渠道退款金额的最小货币单位整数，当前支持CNY分 |
| `merchant_snapshot` | `merchant_snapshot JSON NOT NULL` | 原支付商户配置JSON快照，用于原路退款，敏感配置受控访问 |
| `provider_transaction_id` | `provider_transaction_id VARCHAR(128) NOT NULL` | 原渠道支付交易流水号 |
| `status` | `status VARCHAR(16) NOT NULL DEFAULT 'requested'` | 退款状态：requested待审核、approved已批准、rejected已驳回、cancelled已撤回、dispatching派发中、processing渠道处理中、succeeded成功、closed关闭 |
| `reason` | `reason VARCHAR(200) NOT NULL` | 用户申请退款原因 |
| `review_note` | `review_note VARCHAR(200) NULL` | 管理员审核说明 |
| `reviewed_by` | `reviewed_by CHAR(36) NULL` | 审核管理员用户ID |
| `reviewed_at` | `reviewed_at TIMESTAMP(3) NULL` | 审核时间 |
| `dispatched_at` | `dispatched_at TIMESTAMP(3) NULL` | 占用渠道派发请求的时间，记录后不得当作未派发申请重试 |
| `provider_refund_id` | `provider_refund_id VARCHAR(128) NULL` | 渠道退款流水号 |
| `last_error_code` | `last_error_code VARCHAR(64) NULL` | 最近渠道操作的安全错误码 |
| `wallet_released` | `wallet_released BOOLEAN NOT NULL DEFAULT FALSE` | 退款预留的钱包余额是否已释放，1已释放、0未释放 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 创建时间 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |
| `active_slot` | `active_slot TINYINT GENERATED ALWAYS AS (CASE WHEN status IN('cancelled','rejected') AND dispatched_at IS NULL AND wallet_released=1 THEN NULL ELSE 1 END) STORED` | 自动生成的退款占用槽；撤回或驳回且未派发并已释放钱包预留时为空，其余为1，用于限制同笔支付重复退款 |

## `t_payment_refund_events` — 充值退款审核、派发及渠道结果审计事件

来源：提议新增表；`server/db/migrations/007-refunds.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 退款事件主键 |
| `refund_id` | `refund_id CHAR(32) NOT NULL` | 关联退款单ID |
| `actor_id` | `actor_id CHAR(36) NULL` | 执行操作的用户ID，渠道自动事件可为空 |
| `action` | `action VARCHAR(32) NOT NULL` | 退款操作或渠道结果标识 |
| `detail` | `detail VARCHAR(250) NOT NULL` | 退款事件说明，记录核验结果而非渠道敏感报文 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 事件创建时间 |

## `t_integration_runtime_state` — 外部集成当前运行模式及切换版本

来源：提议新增表；`server/db/migrations/008-coupler-environments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id TINYINT NOT NULL PRIMARY KEY` | 运行状态单例主键，代码使用1 |
| `current_mode` | `current_mode VARCHAR(8) NOT NULL DEFAULT 'SANDBOX'` | 当前集成运行模式：SANDBOX沙箱或LIVE真实环境 |
| `revision` | `revision BIGINT NOT NULL DEFAULT 0` | 运行模式版本号，用于并发变更校验 |
| `updated_by` | `updated_by CHAR(36) NULL` | 最后切换模式的管理员用户ID |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_integration_mode_scopes` — 各运行模式下集成类别的供应商选择与版本

来源：提议新增表；`server/db/migrations/008-coupler-environments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `mode` | `mode VARCHAR(8) NOT NULL` | 集成运行模式：SANDBOX沙箱或LIVE真实环境 |
| `category` | `category VARCHAR(32) NOT NULL` | 集成能力类别代码 |
| `selected_provider` | `selected_provider VARCHAR(32) NULL` | 当前为该类别选择的供应商代码 |
| `revision` | `revision BIGINT NOT NULL DEFAULT 0` | 类别选择状态版本号，用于并发变更校验 |
| `updated_by` | `updated_by CHAR(36) NULL` | 最后更新选择的管理员用户ID |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_integration_mode_configs` — 按运行模式与集成类别隔离的供应商启用配置

来源：提议新增表；`server/db/migrations/008-coupler-environments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `mode` | `mode VARCHAR(8) NOT NULL` | 集成运行模式：SANDBOX沙箱或LIVE真实环境 |
| `category` | `category VARCHAR(32) NOT NULL` | 集成能力类别代码 |
| `provider` | `provider VARCHAR(32) NOT NULL` | 集成供应商代码 |
| `enabled` | `enabled BOOLEAN NOT NULL DEFAULT FALSE` | 供应商在该模式下是否启用，1启用、0停用 |
| `config_revision` | `config_revision BIGINT NOT NULL DEFAULT 0` | 当前引用的供应商配置值版本号 |
| `updated_by` | `updated_by CHAR(36) NULL` | 最后更新配置的管理员用户ID |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 最后更新时间 |

## `t_integration_config_values` — 按模式、供应商和版本隔离的加密集成配置值

来源：提议新增表；`server/db/migrations/008-coupler-environments.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `mode` | `mode VARCHAR(8) NOT NULL` | 集成运行模式：SANDBOX沙箱或LIVE真实环境 |
| `category` | `category VARCHAR(32) NOT NULL` | 集成能力类别代码 |
| `provider` | `provider VARCHAR(32) NOT NULL` | 集成供应商代码 |
| `config_revision` | `config_revision BIGINT NOT NULL` | 该配置值所属版本号 |
| `field_key` | `field_key VARCHAR(64) NOT NULL` | 配置字段键名 |
| `field_cipher` | `field_cipher TEXT NOT NULL` | AES-GCM加密的配置值，认证上下文绑定模式、类别、供应商、版本和字段；禁止作为明文展示 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 加密配置值创建时间 |

## `t_order_escrow_refunds` — 售后裁决退回未花费货款托管资金至买家平台钱包的执行记录

来源：提议新增表；`server/db/migrations/009-order-escrow-refunds.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 托管退款执行主键 |
| `case_id` | `case_id CHAR(36) NOT NULL` | 关联售后争议案件ID |
| `order_id` | `order_id CHAR(36) NOT NULL` | 关联订单ID |
| `buyer_id` | `buyer_id CHAR(36) NOT NULL` | 接收退款的买家用户ID |
| `operator_id` | `operator_id CHAR(36) NOT NULL` | 执行退款的运营管理员用户ID |
| `request_id` | `request_id CHAR(36) NOT NULL` | 执行请求幂等标识 |
| `request_hash` | `request_hash CHAR(64) NOT NULL` | 执行请求内容哈希，用于校验重试参数一致性 |
| `source_round` | `source_round INT NOT NULL` | 退款依据的争议裁决轮次 |
| `currency` | `currency CHAR(3) NOT NULL DEFAULT 'CNY'` | 金额币种代码，当前为CNY |
| `amount` | `amount DECIMAL(12,2) NOT NULL` | 实际退回买家钱包的货款金额，单位元 |
| `original_goods_amount` | `original_goods_amount DECIMAL(12,2) NOT NULL` | 核验的订单原始货款金额，单位元 |
| `service_fee_held` | `service_fee_held DECIMAL(12,2) NOT NULL` | 仍保留托管的服务费金额，单位元 |
| `shipping_held` | `shipping_held DECIMAL(12,2) NOT NULL` | 仍保留托管的运费金额，单位元 |
| `buyer_escrow_before` | `buyer_escrow_before DECIMAL(12,2) NOT NULL` | 退款前买家托管余额，单位元 |
| `buyer_escrow_after` | `buyer_escrow_after DECIMAL(12,2) NOT NULL` | 退款后买家托管余额，单位元 |
| `source_deposit_id` | `source_deposit_id CHAR(36) NOT NULL` | 作为托管资金来源的保证金记录ID |
| `wallet_ledger_id` | `wallet_ledger_id CHAR(36) NOT NULL` | 实际退款入账对应的钱包流水ID |
| `requires_accounting` | `requires_accounting BOOLEAN NOT NULL` | 是否需要进入核算阶段，1表示已退完剩余货款 |
| `source_snapshot` | `source_snapshot JSON NOT NULL` | 核验的托管资金来源JSON快照，用于资金审计，访问应受控 |
| `note` | `note VARCHAR(200) NOT NULL` | 运营执行退款时填写的说明 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 退款执行记录创建时间 |

## `t_order_escrow_refund_events` — 托管货款退款执行的钱包入账审计事件

来源：提议新增表；`server/db/migrations/009-order-escrow-refunds.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) NOT NULL PRIMARY KEY` | 托管退款事件主键 |
| `execution_id` | `execution_id CHAR(36) NOT NULL` | 关联托管退款执行记录ID |
| `actor_id` | `actor_id CHAR(36) NOT NULL` | 执行操作的用户ID |
| `action` | `action VARCHAR(32) NOT NULL` | 执行事件标识，当前写入wallet_refunded表示钱包已退款 |
| `detail` | `detail VARCHAR(250) NOT NULL` | 执行结果说明，用于区分平台钱包退款与外部银行退款 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 事件创建时间 |

## `t_upload_bindings` — 上传文件与使用该文件的领域资源之间的引用绑定

来源：提议新增表；`server/db/migrations/010-upload-bindings.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `upload_id` | `upload_id VARCHAR(36) NOT NULL` | 上传文件记录ID |
| `domain_type` | `domain_type VARCHAR(20) NOT NULL` | 使用文件的领域类型标识 |
| `resource_id` | `resource_id CHAR(36) NOT NULL` | 使用文件的领域资源ID |
| `created_by` | `created_by CHAR(36) NOT NULL` | 建立文件绑定的用户ID |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 绑定创建时间 |

## `t_upload_deletions` — 上传文件删除请求及对象存储删除确认日志

来源：提议新增表；`server/db/migrations/010-upload-bindings.proposed.sql`。

| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |
| --- | --- | --- |
| `id` | `id CHAR(36) PRIMARY KEY` | 文件删除请求主键，由上传文件标识确定 |
| `upload_id` | `upload_id VARCHAR(36) NOT NULL` | 待删除的上传文件记录ID |
| `user_id` | `user_id CHAR(36) NOT NULL` | 发起删除的文件所属用户ID |
| `status` | `status VARCHAR(16) NOT NULL` | 删除状态：deleting删除中或待存储确认、deleted已确认删除 |
| `last_error_code` | `last_error_code VARCHAR(64)` | 对象存储删除确认失败的安全错误码 |
| `created_at` | `created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 删除请求创建时间 |
| `updated_at` | `updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` | 删除状态最后更新时间 |
