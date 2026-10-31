package com.kuaixietech.entity.operations;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_admin_logs")
public class AdminLogs {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("operator_id")
    private String operatorId;
    @TableField("action")
    private String action;
    @TableField("target_type")
    private String targetType;
    @TableField("target_id")
    private String targetId;
    @TableField("detail")
    private String detail;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String value) { this.operatorId = value; }
    public String getAction() { return action; }
    public void setAction(String value) { this.action = value; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String value) { this.targetType = value; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String value) { this.targetId = value; }
    public String getDetail() { return detail; }
    public void setDetail(String value) { this.detail = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
