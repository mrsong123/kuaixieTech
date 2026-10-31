package com.kuaixietech.entity.operations;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_error_logs")
public class ErrorLogs {
    @TableId(value="id", type=IdType.AUTO)
    private Long id;
    @TableField("scope")
    private String scope;
    @TableField("message")
    private String message;
    @TableField("stack")
    private String stack;
    @TableField("page")
    private String page;
    @TableField("user_id")
    private String userId;
    @TableField("extra")
    private String extra;
    @TableField("status")
    private String status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getScope() { return scope; }
    public void setScope(String value) { this.scope = value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }
    public String getStack() { return stack; }
    public void setStack(String value) { this.stack = value; }
    public String getPage() { return page; }
    public void setPage(String value) { this.page = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getExtra() { return extra; }
    public void setExtra(String value) { this.extra = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime value) { this.resolvedAt = value; }
}
