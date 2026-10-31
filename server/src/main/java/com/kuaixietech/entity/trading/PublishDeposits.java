package com.kuaixietech.entity.trading;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_publish_deposits")
public class PublishDeposits {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("status")
    private String status;
    @TableField("source")
    private String source;
    @TableField("order_id")
    private String orderId;
    @TableField("reason")
    private String reason;
    @TableField("refunded_at")
    private LocalDateTime refundedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getSource() { return source; }
    public void setSource(String value) { this.source = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { this.reason = value; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime value) { this.refundedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
