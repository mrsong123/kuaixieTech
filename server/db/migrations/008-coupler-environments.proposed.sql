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
