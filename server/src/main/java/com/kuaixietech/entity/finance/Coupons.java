package com.kuaixietech.entity.finance;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_coupons")
public class Coupons {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("face_value")
    private BigDecimal faceValue;
    @TableField("points_cost")
    private Integer pointsCost;
    @TableField("status")
    private String status;
    @TableField("used_order_id")
    private String usedOrderId;
    @TableField("expire_at")
    private LocalDateTime expireAt;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal value) { this.faceValue = value; }
    public Integer getPointsCost() { return pointsCost; }
    public void setPointsCost(Integer value) { this.pointsCost = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getUsedOrderId() { return usedOrderId; }
    public void setUsedOrderId(String value) { this.usedOrderId = value; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime value) { this.expireAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
