package com.kuaixietech.entity.finance;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_withdrawals")
public class Withdrawals {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("channel")
    private String channel;
    @TableField("status")
    private String status;
    @TableField("admin_note")
    private String adminNote;
    @TableField("reviewed_by")
    private String reviewedBy;
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public String getChannel() { return channel; }
    public void setChannel(String value) { this.channel = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String value) { this.adminNote = value; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String value) { this.reviewedBy = value; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime value) { this.reviewedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
