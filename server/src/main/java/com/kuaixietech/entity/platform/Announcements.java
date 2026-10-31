package com.kuaixietech.entity.platform;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_announcements")
public class Announcements {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("title")
    private String title;
    @TableField("body")
    private String body;
    @TableField("kind")
    private String kind;
    @TableField("pinned")
    private Boolean pinned;
    @TableField("active")
    private Boolean active;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("title_i18n")
    private String titleI18N;
    @TableField("body_i18n")
    private String bodyI18N;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getBody() { return body; }
    public void setBody(String value) { this.body = value; }
    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }
    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean value) { this.pinned = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean value) { this.active = value; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String value) { this.createdBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public String getTitleI18N() { return titleI18N; }
    public void setTitleI18N(String value) { this.titleI18N = value; }
    public String getBodyI18N() { return bodyI18N; }
    public void setBodyI18N(String value) { this.bodyI18N = value; }
}
