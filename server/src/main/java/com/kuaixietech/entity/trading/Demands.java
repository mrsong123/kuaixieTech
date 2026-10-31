package com.kuaixietech.entity.trading;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_demands")
public class Demands {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("title")
    private String title;
    @TableField("item_desc")
    private String itemDesc;
    @TableField("image_urls")
    private String imageUrls;
    @TableField("expect_price")
    private BigDecimal expectPrice;
    @TableField("expect_days")
    private Integer expectDays;
    @TableField("publisher_id")
    private String publisherId;
    @TableField("is_official")
    private Boolean isOfficial;
    @TableField("deadline")
    private LocalDateTime deadline;
    @TableField("status")
    private String status;
    @TableField("awarded_quote_id")
    private String awardedQuoteId;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("pk_mode")
    private Boolean pkMode;
    @TableField("pk_deadline")
    private LocalDateTime pkDeadline;
    @TableField("pk_result")
    private String pkResult;
    @TableField("trip_type")
    private String tripType;
    @TableField("direction_pref")
    private String directionPref;
    @TableField("city")
    private String city;
    @TableField("lat")
    private Double lat;
    @TableField("lng")
    private Double lng;
    @TableField("tax_pref")
    private String taxPref;
    @TableField("insurance_pref")
    private String insurancePref;
    @TableField("shipping_bearer")
    private String shippingBearer;
    @TableField("category_id")
    private Long categoryId;
    @TableField("visibility")
    private String visibility;
    @TableField("matched_trip_id")
    private String matchedTripId;
    @TableField("revealed_at")
    private LocalDateTime revealedAt;
    @TableField("scheduled_at")
    private LocalDateTime scheduledAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String value) { this.itemDesc = value; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String value) { this.imageUrls = value; }
    public BigDecimal getExpectPrice() { return expectPrice; }
    public void setExpectPrice(BigDecimal value) { this.expectPrice = value; }
    public Integer getExpectDays() { return expectDays; }
    public void setExpectDays(Integer value) { this.expectDays = value; }
    public String getPublisherId() { return publisherId; }
    public void setPublisherId(String value) { this.publisherId = value; }
    public Boolean getIsOfficial() { return isOfficial; }
    public void setIsOfficial(Boolean value) { this.isOfficial = value; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime value) { this.deadline = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getAwardedQuoteId() { return awardedQuoteId; }
    public void setAwardedQuoteId(String value) { this.awardedQuoteId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public Boolean getPkMode() { return pkMode; }
    public void setPkMode(Boolean value) { this.pkMode = value; }
    public LocalDateTime getPkDeadline() { return pkDeadline; }
    public void setPkDeadline(LocalDateTime value) { this.pkDeadline = value; }
    public String getPkResult() { return pkResult; }
    public void setPkResult(String value) { this.pkResult = value; }
    public String getTripType() { return tripType; }
    public void setTripType(String value) { this.tripType = value; }
    public String getDirectionPref() { return directionPref; }
    public void setDirectionPref(String value) { this.directionPref = value; }
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }
    public Double getLat() { return lat; }
    public void setLat(Double value) { this.lat = value; }
    public Double getLng() { return lng; }
    public void setLng(Double value) { this.lng = value; }
    public String getTaxPref() { return taxPref; }
    public void setTaxPref(String value) { this.taxPref = value; }
    public String getInsurancePref() { return insurancePref; }
    public void setInsurancePref(String value) { this.insurancePref = value; }
    public String getShippingBearer() { return shippingBearer; }
    public void setShippingBearer(String value) { this.shippingBearer = value; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { this.categoryId = value; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String value) { this.visibility = value; }
    public String getMatchedTripId() { return matchedTripId; }
    public void setMatchedTripId(String value) { this.matchedTripId = value; }
    public LocalDateTime getRevealedAt() { return revealedAt; }
    public void setRevealedAt(LocalDateTime value) { this.revealedAt = value; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime value) { this.scheduledAt = value; }
}
