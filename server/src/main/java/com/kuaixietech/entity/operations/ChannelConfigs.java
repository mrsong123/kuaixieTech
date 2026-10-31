package com.kuaixietech.entity.operations;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_channel_configs")
public class ChannelConfigs {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("channel_type")
    private String channelType;
    @TableField("provider")
    private String provider;
    @TableField("config")
    private String config;
    @TableField("enabled")
    private Boolean enabled;
    @TableField("remark")
    private String remark;
    @TableField("updated_by")
    private String updatedBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String value) { this.channelType = value; }
    public String getProvider() { return provider; }
    public void setProvider(String value) { this.provider = value; }
    public String getConfig() { return config; }
    public void setConfig(String value) { this.config = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { this.enabled = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { this.remark = value; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String value) { this.updatedBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
