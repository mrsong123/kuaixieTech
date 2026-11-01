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
