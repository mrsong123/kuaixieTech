package com.kuaixietech.entity.community;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_post_likes")
public class PostLikes {
    @TableField("post_id")
    private String postId;
    @TableField("user_id")
    private String userId;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getPostId() { return postId; }
    public void setPostId(String value) { this.postId = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
