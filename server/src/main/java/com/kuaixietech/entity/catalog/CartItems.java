package com.kuaixietech.entity.catalog;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_cart_items")
public class CartItems {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("product_id")
    private String productId;
    @TableField("qty")
    private Integer qty;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getProductId() { return productId; }
    public void setProductId(String value) { this.productId = value; }
    public Integer getQty() { return qty; }
    public void setQty(Integer value) { this.qty = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
