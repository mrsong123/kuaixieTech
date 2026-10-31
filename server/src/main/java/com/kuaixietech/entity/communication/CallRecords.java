package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_call_records")
public class CallRecords {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("caller_id")
    private String callerId;
    @TableField("callee_id")
    private String calleeId;
    @TableField("status")
    private String status;
    @TableField("room_id")
    private String roomId;
    @TableField("started_at")
    private LocalDateTime startedAt;
    @TableField("ended_at")
    private LocalDateTime endedAt;
    @TableField("duration_sec")
    private Integer durationSec;
    @TableField("recording_url")
    private String recordingUrl;
    @TableField("transcript_flags")
    private String transcriptFlags;
    @TableField("consent_confirmed")
    private Boolean consentConfirmed;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getCallerId() { return callerId; }
    public void setCallerId(String value) { this.callerId = value; }
    public String getCalleeId() { return calleeId; }
    public void setCalleeId(String value) { this.calleeId = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String value) { this.roomId = value; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime value) { this.startedAt = value; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime value) { this.endedAt = value; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer value) { this.durationSec = value; }
    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String value) { this.recordingUrl = value; }
    public String getTranscriptFlags() { return transcriptFlags; }
    public void setTranscriptFlags(String value) { this.transcriptFlags = value; }
    public Boolean getConsentConfirmed() { return consentConfirmed; }
    public void setConsentConfirmed(Boolean value) { this.consentConfirmed = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
