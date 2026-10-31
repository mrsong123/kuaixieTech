package com.kuaixietech.entity.trip;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_proof_recipes")
public class ProofRecipes {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("trip_type")
    private String tripType;
    @TableField("transport_mode")
    private String transportMode;
    @TableField("country_code")
    private String countryCode;
    @TableField("items")
    private String items;
    @TableField("note")
    private String note;
    @TableField("active")
    private Boolean active;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getTripType() { return tripType; }
    public void setTripType(String value) { this.tripType = value; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String value) { this.transportMode = value; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String value) { this.countryCode = value; }
    public String getItems() { return items; }
    public void setItems(String value) { this.items = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean value) { this.active = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { this.sortOrder = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
