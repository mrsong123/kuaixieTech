package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_notify_outbox")
public class NotifyOutbox {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("order_id")
    private String orderId;
    @TableField("kind")
    private String kind;
    @TableField("title")
    private String title;
    @TableField("body")
    private String body;
    @TableField("channel")
    private String channel;
    @TableField("status")
    private String status;
    @TableField("reason")
    private String reason;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("sent_at")
    private LocalDateTime sentAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getBody() { return body; }
    public void setBody(String value) { this.body = value; }
    public String getChannel() { return channel; }
    public void setChannel(String value) { this.channel = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { this.reason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime value) { this.sentAt = value; }
}
