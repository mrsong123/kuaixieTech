package com.kuaixietech.entity.community;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_post_comments")
public class PostComments {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("post_id")
    private String postId;
    @TableField("author_id")
    private String authorId;
    @TableField("content")
    private String content;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getPostId() { return postId; }
    public void setPostId(String value) { this.postId = value; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String value) { this.authorId = value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
