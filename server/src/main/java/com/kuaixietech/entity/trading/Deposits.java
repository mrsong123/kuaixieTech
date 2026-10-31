package com.kuaixietech.entity.trading;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_deposits")
public class Deposits {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("payer_id")
    private String payerId;
    @TableField("party_role")
    private String partyRole;
    @TableField("kind")
    private String kind;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("status")
    private String status;
    @TableField("payee_id")
    private String payeeId;
    @TableField("reason")
    private String reason;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getPayerId() { return payerId; }
    public void setPayerId(String value) { this.payerId = value; }
    public String getPartyRole() { return partyRole; }
    public void setPartyRole(String value) { this.partyRole = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getPayeeId() { return payeeId; }
    public void setPayeeId(String value) { this.payeeId = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { this.reason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
