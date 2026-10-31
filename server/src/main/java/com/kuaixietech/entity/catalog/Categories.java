package com.kuaixietech.entity.catalog;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_categories")
public class Categories {
    @TableId(value="id", type=IdType.AUTO)
    private Long id;
    @TableField("name")
    private String name;
    @TableField("icon")
    private String icon;
    @TableField("ref_price_min")
    private BigDecimal refPriceMin;
    @TableField("ref_price_max")
    private BigDecimal refPriceMax;
    @TableField("region")
    private String region;
    @TableField("limit_qty")
    private Integer limitQty;
    @TableField("need_permit")
    private Boolean needPermit;
    @TableField("forbidden_note")
    private String forbiddenNote;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("parent_id")
    private Long parentId;
    @TableField("level")
    private Integer level;
    @TableField("active")
    private Boolean active;
    @TableField("country")
    private String country;
    @TableField("name_i18n")
    private String nameI18N;

    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public String getIcon() { return icon; }
    public void setIcon(String value) { this.icon = value; }
    public BigDecimal getRefPriceMin() { return refPriceMin; }
    public void setRefPriceMin(BigDecimal value) { this.refPriceMin = value; }
    public BigDecimal getRefPriceMax() { return refPriceMax; }
    public void setRefPriceMax(BigDecimal value) { this.refPriceMax = value; }
    public String getRegion() { return region; }
    public void setRegion(String value) { this.region = value; }
    public Integer getLimitQty() { return limitQty; }
    public void setLimitQty(Integer value) { this.limitQty = value; }
    public Boolean getNeedPermit() { return needPermit; }
    public void setNeedPermit(Boolean value) { this.needPermit = value; }
    public String getForbiddenNote() { return forbiddenNote; }
    public void setForbiddenNote(String value) { this.forbiddenNote = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { this.sortOrder = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long value) { this.parentId = value; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer value) { this.level = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean value) { this.active = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { this.country = value; }
    public String getNameI18N() { return nameI18N; }
    public void setNameI18N(String value) { this.nameI18N = value; }
}
