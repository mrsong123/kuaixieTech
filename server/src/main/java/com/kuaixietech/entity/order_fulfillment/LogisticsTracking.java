package com.kuaixietech.entity.order_fulfillment;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_logistics_tracking")
public class LogisticsTracking {
    @TableId(value="order_id", type=IdType.INPUT)
    private String orderId;
    @TableField("provider")
    private String provider;
    @TableField("tracking_no")
    private String trackingNo;
    @TableField("latest_state")
    private String latestState;
    @TableField("events_json")
    private String eventsJson;
    @TableField("refreshed_at")
    private LocalDateTime refreshedAt;

    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getProvider() { return provider; }
    public void setProvider(String value) { this.provider = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { this.trackingNo = value; }
    public String getLatestState() { return latestState; }
    public void setLatestState(String value) { this.latestState = value; }
    public String getEventsJson() { return eventsJson; }
    public void setEventsJson(String value) { this.eventsJson = value; }
    public LocalDateTime getRefreshedAt() { return refreshedAt; }
    public void setRefreshedAt(LocalDateTime value) { this.refreshedAt = value; }
}
