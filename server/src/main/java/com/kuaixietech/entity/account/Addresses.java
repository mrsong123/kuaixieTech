package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_addresses")
public class Addresses {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("contact_name")
    private String contactName;
    @TableField("contact_phone")
    private String contactPhone;
    @TableField("province")
    private String province;
    @TableField("city")
    private String city;
    @TableField("district")
    private String district;
    @TableField("detail")
    private String detail;
    @TableField("is_default")
    private Boolean isDefault;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getContactName() { return contactName; }
    public void setContactName(String value) { this.contactName = value; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String value) { this.contactPhone = value; }
    public String getProvince() { return province; }
    public void setProvince(String value) { this.province = value; }
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }
    public String getDistrict() { return district; }
    public void setDistrict(String value) { this.district = value; }
    public String getDetail() { return detail; }
    public void setDetail(String value) { this.detail = value; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean value) { this.isDefault = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
