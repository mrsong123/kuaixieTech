package com.kuaixietech.entity.finance;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_invoices")
public class Invoices {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    @TableField("user_id")
    private String userId;
    @TableField("title_type")
    private String titleType;
    @TableField("title_name")
    private String titleName;
    @TableField("tax_no")
    private String taxNo;
    @TableField("invoice_kind")
    private String invoiceKind;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("email")
    private String email;
    @TableField("remark")
    private String remark;
    @TableField("status")
    private String status;
    @TableField("invoice_no")
    private String invoiceNo;
    @TableField("file_url")
    private String fileUrl;
    @TableField("issued_at")
    private LocalDateTime issuedAt;
    @TableField("reject_reason")
    private String rejectReason;
    @TableField("created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { this.orderId = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public String getTitleType() { return titleType; }
    public void setTitleType(String value) { this.titleType = value; }
    public String getTitleName() { return titleName; }
    public void setTitleName(String value) { this.titleName = value; }
    public String getTaxNo() { return taxNo; }
    public void setTaxNo(String value) { this.taxNo = value; }
    public String getInvoiceKind() { return invoiceKind; }
    public void setInvoiceKind(String value) { this.invoiceKind = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { this.remark = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String value) { this.invoiceNo = value; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String value) { this.fileUrl = value; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime value) { this.issuedAt = value; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String value) { this.rejectReason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
