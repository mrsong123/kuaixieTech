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
