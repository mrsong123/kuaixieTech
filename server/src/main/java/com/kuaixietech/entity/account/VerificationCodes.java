package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_verification_codes")
public class VerificationCodes {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("target")
    private String target;
    @TableField("channel")
    private String channel;
    @TableField("code")
    private String code;
    @TableField("purpose")
    private String purpose;
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    @TableField("used")
    private Boolean used;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getTarget() { return target; }
    public void setTarget(String value) { this.target = value; }
    public String getChannel() { return channel; }
    public void setChannel(String value) { this.channel = value; }
    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String value) { this.purpose = value; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime value) { this.expiresAt = value; }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean value) { this.used = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
