package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_notifications")
public class Notifications {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("kind")
    private String kind;
    @TableField("title")
    private String title;
    @TableField("body")
    private String body;
    @TableField("order_id")
    private String orderId;
    @TableField("read_flag")
    private Boolean readFlag;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getBody() { return body; }
    public void setBody(String value) { this.body = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public Boolean getReadFlag() { return readFlag; }
    public void setReadFlag(Boolean value) { this.readFlag = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
