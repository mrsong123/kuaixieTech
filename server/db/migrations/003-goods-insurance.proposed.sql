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
