package com.kuaixietech.entity.platform;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_legal_docs")
public class LegalDocs {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("doc_key")
    private String docKey;
    @TableField("title")
    private String title;
    @TableField("body_md")
    private String bodyMd;
    @TableField("version")
    private String version;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("title_i18n")
    private String titleI18N;
    @TableField("body_md_i18n")
    private String bodyMdI18N;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getDocKey() { return docKey; }
    public void setDocKey(String value) { this.docKey = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getBodyMd() { return bodyMd; }
    public void setBodyMd(String value) { this.bodyMd = value; }
    public String getVersion() { return version; }
    public void setVersion(String value) { this.version = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public String getTitleI18N() { return titleI18N; }
    public void setTitleI18N(String value) { this.titleI18N = value; }
    public String getBodyMdI18N() { return bodyMdI18N; }
    public void setBodyMdI18N(String value) { this.bodyMdI18N = value; }
}
