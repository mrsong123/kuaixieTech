package com.kuaixietech.entity.internationalization;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_locales")
public class Locales {
    @TableId(value="code", type=IdType.INPUT)
    private String code;
    @TableField("native_name")
    private String nativeName;
    @TableField("label_zh")
    private String labelZh;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("active")
    private Boolean active;
    @TableField("fallback_code")
    private String fallbackCode;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getNativeName() { return nativeName; }
    public void setNativeName(String value) { this.nativeName = value; }
    public String getLabelZh() { return labelZh; }
    public void setLabelZh(String value) { this.labelZh = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { this.sortOrder = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean value) { this.active = value; }
    public String getFallbackCode() { return fallbackCode; }
    public void setFallbackCode(String value) { this.fallbackCode = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
