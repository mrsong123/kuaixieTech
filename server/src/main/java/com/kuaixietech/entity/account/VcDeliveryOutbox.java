package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_vc_delivery_outbox")
public class VcDeliveryOutbox {
    @TableId(value="id", type=IdType.AUTO)
    private Long id;
    @TableField("target")
    private String target;
    @TableField("channel")
    private String channel;
    @TableField("code")
    private String code;
    @TableField("status")
    private String status;
    @TableField("error")
    private String error;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getTarget() { return target; }
    public void setTarget(String value) { this.target = value; }
    public String getChannel() { return channel; }
    public void setChannel(String value) { this.channel = value; }
    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getError() { return error; }
    public void setError(String value) { this.error = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
