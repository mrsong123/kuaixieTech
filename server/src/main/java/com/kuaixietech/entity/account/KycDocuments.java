package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_kyc_documents")
public class KycDocuments {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("doc_type")
    private String docType;
    @TableField("storage_url")
    private String storageUrl;
    @TableField("status")
    private String status;
    @TableField("reject_reason")
    private String rejectReason;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("challenge_code")
    private String challengeCode;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getDocType() { return docType; }
    public void setDocType(String value) { this.docType = value; }
    public String getStorageUrl() { return storageUrl; }
    public void setStorageUrl(String value) { this.storageUrl = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String value) { this.rejectReason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public String getChallengeCode() { return challengeCode; }
    public void setChallengeCode(String value) { this.challengeCode = value; }
}
