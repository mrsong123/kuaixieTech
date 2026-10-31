package com.kuaixietech.entity.trip;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_trip_categories")
public class TripCategories {
    @TableField("trip_id")
    private String tripId;
    @TableField("category_id")
    private Long categoryId;

    public String getTripId() { return tripId; }
    public void setTripId(String value) { this.tripId = value; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { this.categoryId = value; }
}
