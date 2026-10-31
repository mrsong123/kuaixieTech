package com.kuaixietech.entity.community;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_chat_violations")
public class ChatViolations {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("code")
    private String code;
    @TableField("snippet")
    private String snippet;
    @TableField("reviewed")
    private Boolean reviewed;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String value) { this.snippet = value; }
    public Boolean getReviewed() { return reviewed; }
    public void setReviewed(Boolean value) { this.reviewed = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
