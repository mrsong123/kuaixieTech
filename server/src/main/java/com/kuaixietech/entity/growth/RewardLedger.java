package com.kuaixietech.entity.growth;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_reward_ledger")
public class RewardLedger {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("inviter_id")
    private String inviterId;
    @TableField("invitee_id")
    private String inviteeId;
    @TableField("order_id")
    private String orderId;
    @TableField("level")
    private Short level;
    @TableField("event_type")
    private String eventType;
    @TableField("reward_kind")
    private String rewardKind;
    @TableField("amount")
    private Integer amount;
    @TableField("status")
    private String status;
    @TableField("note")
    private String note;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("rate")
    private BigDecimal rate;
    @TableField("base_amount")
    private BigDecimal baseAmount;
    @TableField("currency")
    private String currency;
    @TableField("settled_to_balance")
    private Boolean settledToBalance;
    @TableField("revoked_at")
    private LocalDateTime revokedAt;
    @TableField("revoked_reason")
    private String revokedReason;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getInviterId() { return inviterId; }
    public void setInviterId(String value) { this.inviterId = value; }
    public String getInviteeId() { return inviteeId; }
    public void setInviteeId(String value) { this.inviteeId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public Short getLevel() { return level; }
    public void setLevel(Short value) { this.level = value; }
    public String getEventType() { return eventType; }
    public void setEventType(String value) { this.eventType = value; }
    public String getRewardKind() { return rewardKind; }
    public void setRewardKind(String value) { this.rewardKind = value; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer value) { this.amount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal value) { this.rate = value; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal value) { this.baseAmount = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String value) { this.currency = value; }
    public Boolean getSettledToBalance() { return settledToBalance; }
    public void setSettledToBalance(Boolean value) { this.settledToBalance = value; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime value) { this.revokedAt = value; }
    public String getRevokedReason() { return revokedReason; }
    public void setRevokedReason(String value) { this.revokedReason = value; }
}
