package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_support_messages")
public class SupportMessages {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("thread_id")
    private String threadId;
    @TableField("sender_id")
    private String senderId;
    @TableField("sender_role")
    private String senderRole;
    @TableField("content")
    private String content;
    @TableField("image_urls")
    private String imageUrls;
    @TableField("read_flag")
    private Boolean readFlag;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getThreadId() { return threadId; }
    public void setThreadId(String value) { this.threadId = value; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String value) { this.senderId = value; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String value) { this.senderRole = value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String value) { this.imageUrls = value; }
    public Boolean getReadFlag() { return readFlag; }
    public void setReadFlag(Boolean value) { this.readFlag = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
