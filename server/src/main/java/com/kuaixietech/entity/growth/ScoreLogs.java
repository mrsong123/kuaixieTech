package com.kuaixietech.entity.growth;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_score_logs")
public class ScoreLogs {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("order_id")
    private String orderId;
    @TableField("event")
    private String event;
    @TableField("delta")
    private Integer delta;
    @TableField("score_after")
    private Integer scoreAfter;
    @TableField("note")
    private String note;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getEvent() { return event; }
    public void setEvent(String value) { this.event = value; }
    public Integer getDelta() { return delta; }
    public void setDelta(Integer value) { this.delta = value; }
    public Integer getScoreAfter() { return scoreAfter; }
    public void setScoreAfter(Integer value) { this.scoreAfter = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
