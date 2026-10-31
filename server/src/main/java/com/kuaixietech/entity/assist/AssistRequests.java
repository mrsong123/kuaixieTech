package com.kuaixietech.entity.assist;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_assist_requests")
public class AssistRequests {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("requester_id")
    private String requesterId;
    @TableField("assistant_id")
    private String assistantId;
    @TableField("share_rate")
    private BigDecimal shareRate;
    @TableField("note")
    private String note;
    @TableField("status")
    private String status;
    @TableField("handover_a_urls")
    private String handoverAUrls;
    @TableField("handover_a_city")
    private String handoverACity;
    @TableField("handover_a_at")
    private LocalDateTime handoverAAt;
    @TableField("handover_b_urls")
    private String handoverBUrls;
    @TableField("handover_b_city")
    private String handoverBCity;
    @TableField("finished_at")
    private LocalDateTime finishedAt;
    @TableField("settled_amount")
    private BigDecimal settledAmount;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String value) { this.requesterId = value; }
    public String getAssistantId() { return assistantId; }
    public void setAssistantId(String value) { this.assistantId = value; }
    public BigDecimal getShareRate() { return shareRate; }
    public void setShareRate(BigDecimal value) { this.shareRate = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getHandoverAUrls() { return handoverAUrls; }
    public void setHandoverAUrls(String value) { this.handoverAUrls = value; }
    public String getHandoverACity() { return handoverACity; }
    public void setHandoverACity(String value) { this.handoverACity = value; }
    public LocalDateTime getHandoverAAt() { return handoverAAt; }
    public void setHandoverAAt(LocalDateTime value) { this.handoverAAt = value; }
    public String getHandoverBUrls() { return handoverBUrls; }
    public void setHandoverBUrls(String value) { this.handoverBUrls = value; }
    public String getHandoverBCity() { return handoverBCity; }
    public void setHandoverBCity(String value) { this.handoverBCity = value; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime value) { this.finishedAt = value; }
    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal value) { this.settledAmount = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
