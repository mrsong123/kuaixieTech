-- Deployment-only proposal: isolated API configuration environments.
-- No startup DDL, no copying legacy plaintext/ciphertext, no business balance or payment mutation.
CREATE TABLE IF NOT EXISTS t_integration_runtime_state (
 id TINYINT NOT NULL PRIMARY KEY,
 current_mode VARCHAR(8) NOT NULL DEFAULT 'SANDBOX',
 revision BIGINT NOT NULL DEFAULT 0,
 updated_by CHAR(36) NULL,
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 CONSTRAINT chk_coupler_runtime_id CHECK(id=1),
 CONSTRAINT chk_coupler_runtime_mode CHECK(current_mode IN('SANDBOX','LIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_integration_mode_scopes (
 mode VARCHAR(8) NOT NULL,
 category VARCHAR(32) NOT NULL,
 selected_provider VARCHAR(32) NULL,
 revision BIGINT NOT NULL DEFAULT 0,
 updated_by CHAR(36) NULL,
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 PRIMARY KEY(mode,category),
 CONSTRAINT chk_coupler_scope_mode CHECK(mode IN('SANDBOX','LIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_integration_mode_configs (
 mode VARCHAR(8) NOT NULL,
 category VARCHAR(32) NOT NULL,
 provider VARCHAR(32) NOT NULL,
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 config_revision BIGINT NOT NULL DEFAULT 0,
 updated_by CHAR(36) NULL,
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 PRIMARY KEY(mode,category,provider),
 CONSTRAINT chk_coupler_config_mode CHECK(mode IN('SANDBOX','LIVE')),
 CONSTRAINT fk_coupler_config_scope FOREIGN KEY(mode,category) REFERENCES t_integration_mode_scopes(mode,category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_integration_config_values (
 mode VARCHAR(8) NOT NULL,
 category VARCHAR(32) NOT NULL,
 provider VARCHAR(32) NOT NULL,
 config_revision BIGINT NOT NULL,
 field_key VARCHAR(64) NOT NULL,
 field_cipher TEXT NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(mode,category,provider,config_revision,field_key),
 CONSTRAINT chk_coupler_value_mode CHECK(mode IN('SANDBOX','LIVE')),
 CONSTRAINT chk_coupler_value_revision CHECK(config_revision>0),
 CONSTRAINT fk_coupler_value_config FOREIGN KEY(mode,category,provider) REFERENCES t_integration_mode_configs(mode,category,provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
