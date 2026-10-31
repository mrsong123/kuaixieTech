package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_support_tickets")
public class SupportTickets {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("order_id")
    private String orderId;
    @TableField("topic")
    private String topic;
    @TableField("content")
    private String content;
    @TableField("status")
    private String status;
    @TableField("handler_id")
    private String handlerId;
    @TableField("reply")
    private String reply;
    @TableField("last_reply_at")
    private LocalDateTime lastReplyAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getTopic() { return topic; }
    public void setTopic(String value) { this.topic = value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getHandlerId() { return handlerId; }
    public void setHandlerId(String value) { this.handlerId = value; }
    public String getReply() { return reply; }
    public void setReply(String value) { this.reply = value; }
    public LocalDateTime getLastReplyAt() { return lastReplyAt; }
    public void setLastReplyAt(LocalDateTime value) { this.lastReplyAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
