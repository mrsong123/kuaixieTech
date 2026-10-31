package com.kuaixietech.entity.order_fulfillment;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_cancel_requests")
public class CancelRequests {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("applicant_id")
    private String applicantId;
    @TableField("applicant_role")
    private String applicantRole;
    @TableField("reason_type")
    private String reasonType;
    @TableField("reason_detail")
    private String reasonDetail;
    @TableField("proof_urls")
    private String proofUrls;
    @TableField("claim_force_majeure")
    private Boolean claimForceMajeure;
    @TableField("mutual_plan")
    private String mutualPlan;
    @TableField("status")
    private String status;
    @TableField("verdict")
    private String verdict;
    @TableField("penalty_amount")
    private BigDecimal penaltyAmount;
    @TableField("verdict_reason")
    private String verdictReason;
    @TableField("withdrawn")
    private Boolean withdrawn;
    @TableField("reviewed_by")
    private String reviewedBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String value) { this.applicantId = value; }
    public String getApplicantRole() { return applicantRole; }
    public void setApplicantRole(String value) { this.applicantRole = value; }
    public String getReasonType() { return reasonType; }
    public void setReasonType(String value) { this.reasonType = value; }
    public String getReasonDetail() { return reasonDetail; }
    public void setReasonDetail(String value) { this.reasonDetail = value; }
    public String getProofUrls() { return proofUrls; }
    public void setProofUrls(String value) { this.proofUrls = value; }
    public Boolean getClaimForceMajeure() { return claimForceMajeure; }
    public void setClaimForceMajeure(Boolean value) { this.claimForceMajeure = value; }
    public String getMutualPlan() { return mutualPlan; }
    public void setMutualPlan(String value) { this.mutualPlan = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getVerdict() { return verdict; }
    public void setVerdict(String value) { this.verdict = value; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal value) { this.penaltyAmount = value; }
    public String getVerdictReason() { return verdictReason; }
    public void setVerdictReason(String value) { this.verdictReason = value; }
    public Boolean getWithdrawn() { return withdrawn; }
    public void setWithdrawn(Boolean value) { this.withdrawn = value; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String value) { this.reviewedBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime value) { this.resolvedAt = value; }
}
