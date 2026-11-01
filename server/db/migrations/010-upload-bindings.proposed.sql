-- Deployment proposal only. Keep private objects private; no business rows are backfilled or deleted.
CREATE TABLE IF NOT EXISTS t_upload_bindings (
 upload_id VARCHAR(36) NOT NULL,
 domain_type VARCHAR(20) NOT NULL,
 resource_id CHAR(36) NOT NULL,
 created_by CHAR(36) NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(upload_id,domain_type,resource_id),
 INDEX idx_upload_binding_resource(domain_type,resource_id),
 CONSTRAINT chk_upload_binding_domain CHECK(domain_type IN('product','order','dispute'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS t_upload_deletions (
 id CHAR(36) PRIMARY KEY,
 upload_id VARCHAR(36) NOT NULL,
 user_id CHAR(36) NOT NULL,
 status VARCHAR(16) NOT NULL,
 last_error_code VARCHAR(64),
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uq_upload_deletion(upload_id),
 INDEX idx_upload_deletion_user(user_id,created_at),
 CONSTRAINT chk_upload_deletion_status CHECK(status IN('deleting','deleted'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
