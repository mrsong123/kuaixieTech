package com.kuaixietech.entity.growth;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_reviews")
public class Reviews {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("reviewer_id")
    private String reviewerId;
    @TableField("target_id")
    private String targetId;
    @TableField("stars")
    private Integer stars;
    @TableField("tags")
    private String tags;
    @TableField("content")
    private String content;
    @TableField("hidden")
    private Boolean hidden;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String value) { this.reviewerId = value; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String value) { this.targetId = value; }
    public Integer getStars() { return stars; }
    public void setStars(Integer value) { this.stars = value; }
    public String getTags() { return tags; }
    public void setTags(String value) { this.tags = value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean value) { this.hidden = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
