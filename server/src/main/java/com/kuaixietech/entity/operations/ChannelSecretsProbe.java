package com.kuaixietech.entity.operations;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_channel_secrets_probe")
public class ChannelSecretsProbe {
    @TableId(value="channel", type=IdType.INPUT)
    private String channel;
    @TableField("ready")
    private Boolean ready;
    @TableField("probed_at")
    private LocalDateTime probedAt;

    public String getChannel() { return channel; }
    public void setChannel(String value) { this.channel = value; }
    public Boolean getReady() { return ready; }
    public void setReady(Boolean value) { this.ready = value; }
    public LocalDateTime getProbedAt() { return probedAt; }
    public void setProbedAt(LocalDateTime value) { this.probedAt = value; }
}
