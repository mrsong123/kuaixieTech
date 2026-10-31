package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_privacy_numbers")
public class PrivacyNumbers {
    @TableId(value="virtual_number", type=IdType.INPUT)
    private String virtualNumber;
    @TableField("pool_code")
    private String poolCode;
    @TableField("status")
    private String status;
    @TableField("remark")
    private String remark;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getVirtualNumber() { return virtualNumber; }
    public void setVirtualNumber(String value) { this.virtualNumber = value; }
    public String getPoolCode() { return poolCode; }
    public void setPoolCode(String value) { this.poolCode = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { this.remark = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
