package com.kuaixietech.entity.finance;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_pay_orders")
public class PayOrders {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("out_trade_no")
    private String outTradeNo;
    @TableField("prepay_id")
    private String prepayId;
    @TableField("transaction_id")
    private String transactionId;
    @TableField("amount_cents")
    private Integer amountCents;
    @TableField("scene")
    private String scene;
    @TableField("status")
    private String status;
    @TableField("refund_no")
    private String refundNo;
    @TableField("paid_at")
    private LocalDateTime paidAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String value) { this.outTradeNo = value; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String value) { this.prepayId = value; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String value) { this.transactionId = value; }
    public Integer getAmountCents() { return amountCents; }
    public void setAmountCents(Integer value) { this.amountCents = value; }
    public String getScene() { return scene; }
    public void setScene(String value) { this.scene = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getRefundNo() { return refundNo; }
    public void setRefundNo(String value) { this.refundNo = value; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime value) { this.paidAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
