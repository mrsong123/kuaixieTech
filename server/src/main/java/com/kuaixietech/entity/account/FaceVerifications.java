package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_face_verifications")
public class FaceVerifications {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("scene")
    private String scene;
    @TableField("snapshot_url")
    private String snapshotUrl;
    @TableField("device_info")
    private String deviceInfo;
    @TableField("result")
    private String result;
    @TableField("order_id")
    private String orderId;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("challenge_code")
    private String challengeCode;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getScene() { return scene; }
    public void setScene(String value) { this.scene = value; }
    public String getSnapshotUrl() { return snapshotUrl; }
    public void setSnapshotUrl(String value) { this.snapshotUrl = value; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String value) { this.deviceInfo = value; }
    public String getResult() { return result; }
    public void setResult(String value) { this.result = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public String getChallengeCode() { return challengeCode; }
    public void setChallengeCode(String value) { this.challengeCode = value; }
}
