package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_join_applications")
public class JoinApplications {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("operator_id")
    private String operatorId;
    @TableField("target_username")
    private String targetUsername;
    @TableField("target_user_id")
    private String targetUserId;
    @TableField("note")
    private String note;
    @TableField("status")
    private String status;
    @TableField("responded_at")
    private LocalDateTime respondedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String value) { this.operatorId = value; }
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String value) { this.targetUsername = value; }
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String value) { this.targetUserId = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime value) { this.respondedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
