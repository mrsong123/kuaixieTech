package com.kuaixietech.entity.catalog;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_category_countries")
public class CategoryCountries {
    @TableField("category_id")
    private Long categoryId;
    @TableField("country_code")
    private String countryCode;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { this.categoryId = value; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String value) { this.countryCode = value; }
}
