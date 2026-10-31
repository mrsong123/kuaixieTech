package com.kuaixietech.entity.finance;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_balance_ledger")
public class BalanceLedger {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("kind")
    private String kind;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("balance_after")
    private BigDecimal balanceAfter;
    @TableField("order_id")
    private String orderId;
    @TableField("note")
    private String note;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal value) { this.balanceAfter = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
