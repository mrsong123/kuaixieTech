package com.kuaixietech.entity.order_fulfillment;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_order_logistics")
public class OrderLogistics {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("node")
    private String node;
    @TableField("operator_id")
    private String operatorId;
    @TableField("operator_role")
    private String operatorRole;
    @TableField("detail")
    private String detail;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getNode() { return node; }
    public void setNode(String value) { this.node = value; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String value) { this.operatorId = value; }
    public String getOperatorRole() { return operatorRole; }
    public void setOperatorRole(String value) { this.operatorRole = value; }
    public String getDetail() { return detail; }
    public void setDetail(String value) { this.detail = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
