package com.kuaixietech.entity.trading;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_quotes")
public class Quotes {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("demand_id")
    private String demandId;
    @TableField("vendor_id")
    private String vendorId;
    @TableField("trip_id")
    private String tripId;
    @TableField("price")
    private BigDecimal price;
    @TableField("promise_days")
    private Integer promiseDays;
    @TableField("purchase_place")
    private String purchasePlace;
    @TableField("note")
    private String note;
    @TableField("status")
    private String status;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getDemandId() { return demandId; }
    public void setDemandId(String value) { this.demandId = value; }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String value) { this.vendorId = value; }
    public String getTripId() { return tripId; }
    public void setTripId(String value) { this.tripId = value; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { this.price = value; }
    public Integer getPromiseDays() { return promiseDays; }
    public void setPromiseDays(Integer value) { this.promiseDays = value; }
    public String getPurchasePlace() { return purchasePlace; }
    public void setPurchasePlace(String value) { this.purchasePlace = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
