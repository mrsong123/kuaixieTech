package com.kuaixietech.entity.growth;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_invite_codes")
public class InviteCodes {
    @TableId(value="code", type=IdType.INPUT)
    private String code;
    @TableField("note")
    private String note;
    @TableField("used_by")
    private String usedBy;
    @TableField("used_at")
    private LocalDateTime usedAt;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getUsedBy() { return usedBy; }
    public void setUsedBy(String value) { this.usedBy = value; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime value) { this.usedAt = value; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String value) { this.createdBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
