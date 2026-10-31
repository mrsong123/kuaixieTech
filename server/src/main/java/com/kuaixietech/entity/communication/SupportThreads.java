package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_support_threads")
public class SupportThreads {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("kind")
    private String kind;
    @TableField("user_id")
    private String userId;
    @TableField("peer_id")
    private String peerId;
    @TableField("order_id")
    private String orderId;
    @TableField("status")
    private String status;
    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getPeerId() { return peerId; }
    public void setPeerId(String value) { this.peerId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime value) { this.lastMessageAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
