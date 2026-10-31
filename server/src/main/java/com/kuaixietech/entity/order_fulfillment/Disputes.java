package com.kuaixietech.entity.order_fulfillment;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_disputes")
public class Disputes {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("initiator_id")
    private String initiatorId;
    @TableField("respondent_id")
    private String respondentId;
    @TableField("type")
    private String type;
    @TableField("expect_resolution")
    private String expectResolution;
    @TableField("description")
    private String description;
    @TableField("evidence_urls")
    private String evidenceUrls;
    @TableField("respondent_evidence_urls")
    private String respondentEvidenceUrls;
    @TableField("respondent_note")
    private String respondentNote;
    @TableField("stage")
    private String stage;
    @TableField("round")
    private Integer round;
    @TableField("verdict")
    private String verdict;
    @TableField("refund_amount")
    private BigDecimal refundAmount;
    @TableField("deposit_owner")
    private String depositOwner;
    @TableField("verdict_reason")
    private String verdictReason;
    @TableField("reviewed_by")
    private String reviewedBy;
    @TableField("reopened_used")
    private Boolean reopenedUsed;
    @TableField("review_appeal")
    private String reviewAppeal;
    @TableField("review_result")
    private String reviewResult;
    @TableField("negotiation_deadline")
    private LocalDateTime negotiationDeadline;
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getInitiatorId() { return initiatorId; }
    public void setInitiatorId(String value) { this.initiatorId = value; }
    public String getRespondentId() { return respondentId; }
    public void setRespondentId(String value) { this.respondentId = value; }
    public String getType() { return type; }
    public void setType(String value) { this.type = value; }
    public String getExpectResolution() { return expectResolution; }
    public void setExpectResolution(String value) { this.expectResolution = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    public String getEvidenceUrls() { return evidenceUrls; }
    public void setEvidenceUrls(String value) { this.evidenceUrls = value; }
    public String getRespondentEvidenceUrls() { return respondentEvidenceUrls; }
    public void setRespondentEvidenceUrls(String value) { this.respondentEvidenceUrls = value; }
    public String getRespondentNote() { return respondentNote; }
    public void setRespondentNote(String value) { this.respondentNote = value; }
    public String getStage() { return stage; }
    public void setStage(String value) { this.stage = value; }
    public Integer getRound() { return round; }
    public void setRound(Integer value) { this.round = value; }
    public String getVerdict() { return verdict; }
    public void setVerdict(String value) { this.verdict = value; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal value) { this.refundAmount = value; }
    public String getDepositOwner() { return depositOwner; }
    public void setDepositOwner(String value) { this.depositOwner = value; }
    public String getVerdictReason() { return verdictReason; }
    public void setVerdictReason(String value) { this.verdictReason = value; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String value) { this.reviewedBy = value; }
    public Boolean getReopenedUsed() { return reopenedUsed; }
    public void setReopenedUsed(Boolean value) { this.reopenedUsed = value; }
    public String getReviewAppeal() { return reviewAppeal; }
    public void setReviewAppeal(String value) { this.reviewAppeal = value; }
    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String value) { this.reviewResult = value; }
    public LocalDateTime getNegotiationDeadline() { return negotiationDeadline; }
    public void setNegotiationDeadline(LocalDateTime value) { this.negotiationDeadline = value; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime value) { this.resolvedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
