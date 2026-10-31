package com.kuaixietech.entity.catalog;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_products")
public class Products {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("owner_id")
    private String ownerId;
    @TableField("category_id")
    private Long categoryId;
    @TableField("name")
    private String name;
    @TableField("spec")
    private String spec;
    @TableField("region")
    private String region;
    @TableField("price")
    private BigDecimal price;
    @TableField("stock")
    private Integer stock;
    @TableField("image_urls")
    private String imageUrls;
    @TableField("note")
    private String note;
    @TableField("status")
    private String status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("name_i18n")
    private String nameI18N;
    @TableField("spec_i18n")
    private String specI18N;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String value) { this.ownerId = value; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { this.categoryId = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public String getSpec() { return spec; }
    public void setSpec(String value) { this.spec = value; }
    public String getRegion() { return region; }
    public void setRegion(String value) { this.region = value; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { this.price = value; }
    public Integer getStock() { return stock; }
    public void setStock(Integer value) { this.stock = value; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String value) { this.imageUrls = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
    public String getNameI18N() { return nameI18N; }
    public void setNameI18N(String value) { this.nameI18N = value; }
    public String getSpecI18N() { return specI18N; }
    public void setSpecI18N(String value) { this.specI18N = value; }
}
