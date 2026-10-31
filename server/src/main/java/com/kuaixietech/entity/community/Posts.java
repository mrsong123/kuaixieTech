package com.kuaixietech.entity.community;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_posts")
public class Posts {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("author_id")
    private String authorId;
    @TableField("kind")
    private String kind;
    @TableField("title")
    private String title;
    @TableField("body")
    private String body;
    @TableField("image_urls")
    private String imageUrls;
    @TableField("order_ref")
    private String orderRef;
    @TableField("like_count")
    private Integer likeCount;
    @TableField("comment_count")
    private Integer commentCount;
    @TableField("status")
    private String status;
    @TableField("removed_reason")
    private String removedReason;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String value) { this.authorId = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getBody() { return body; }
    public void setBody(String value) { this.body = value; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String value) { this.imageUrls = value; }
    public String getOrderRef() { return orderRef; }
    public void setOrderRef(String value) { this.orderRef = value; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer value) { this.likeCount = value; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer value) { this.commentCount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getRemovedReason() { return removedReason; }
    public void setRemovedReason(String value) { this.removedReason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
