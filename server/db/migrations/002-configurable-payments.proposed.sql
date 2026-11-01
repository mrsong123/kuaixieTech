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
