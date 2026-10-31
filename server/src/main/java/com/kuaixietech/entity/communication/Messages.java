package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_messages")
public class Messages {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("sender_id")
    private String senderId;
    @TableField("content")
    private String content;
    @TableField("image_urls")
    private String imageUrls;
    @TableField("system_flag")
    private Boolean systemFlag;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("sender_role")
    private String senderRole;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String value) { this.senderId = value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String value) { this.imageUrls = value; }
    public Boolean getSystemFlag() { return systemFlag; }
    public void setSystemFlag(Boolean value) { this.systemFlag = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String value) { this.senderRole = value; }
}
