package com.kuaixietech.entity.communication;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_call_logs")
public class CallLogs {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("binding_id")
    private String bindingId;
    @TableField("caller_id")
    private String callerId;
    @TableField("caller_role")
    private String callerRole;
    @TableField("peer_role")
    private String peerRole;
    @TableField("virtual_number")
    private String virtualNumber;
    @TableField("result")
    private String result;
    @TableField("duration_sec")
    private Integer durationSec;
    @TableField("note")
    private String note;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getBindingId() { return bindingId; }
    public void setBindingId(String value) { this.bindingId = value; }
    public String getCallerId() { return callerId; }
    public void setCallerId(String value) { this.callerId = value; }
    public String getCallerRole() { return callerRole; }
    public void setCallerRole(String value) { this.callerRole = value; }
    public String getPeerRole() { return peerRole; }
    public void setPeerRole(String value) { this.peerRole = value; }
    public String getVirtualNumber() { return virtualNumber; }
    public void setVirtualNumber(String value) { this.virtualNumber = value; }
    public String getResult() { return result; }
    public void setResult(String value) { this.result = value; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer value) { this.durationSec = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
