package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_privacy_bindings")
public class PrivacyBindings {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("virtual_number")
    private String virtualNumber;
    @TableField("buyer_phone_masked")
    private String buyerPhoneMasked;
    @TableField("vendor_phone_masked")
    private String vendorPhoneMasked;
    @TableField("aliyun_sub_id")
    private String aliyunSubId;
    @TableField("expire_at")
    private LocalDateTime expireAt;
    @TableField("status")
    private String status;
    @TableField("release_reason")
    private String releaseReason;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("released_at")
    private LocalDateTime releasedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getVirtualNumber() { return virtualNumber; }
    public void setVirtualNumber(String value) { this.virtualNumber = value; }
    public String getBuyerPhoneMasked() { return buyerPhoneMasked; }
    public void setBuyerPhoneMasked(String value) { this.buyerPhoneMasked = value; }
    public String getVendorPhoneMasked() { return vendorPhoneMasked; }
    public void setVendorPhoneMasked(String value) { this.vendorPhoneMasked = value; }
    public String getAliyunSubId() { return aliyunSubId; }
    public void setAliyunSubId(String value) { this.aliyunSubId = value; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime value) { this.expireAt = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getReleaseReason() { return releaseReason; }
    public void setReleaseReason(String value) { this.releaseReason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime value) { this.releasedAt = value; }
}
