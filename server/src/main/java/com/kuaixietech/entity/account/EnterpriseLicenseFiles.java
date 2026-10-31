package com.kuaixietech.entity.account;
import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
@TableName("t_enterprise_license_files")
public class EnterpriseLicenseFiles {
 @TableId(value="id",type=IdType.INPUT) private String id;
 @TableField("user_id") private String userId;
 @TableField("storage_url") private String storageUrl;
 @TableField("position_no") private Integer positionNo;
 @TableField("created_at") private LocalDateTime createdAt;
 public String getId(){return id;} public void setId(String value){this.id=value;}
 public String getUserId(){return userId;} public void setUserId(String value){this.userId=value;}
 public String getStorageUrl(){return storageUrl;} public void setStorageUrl(String value){this.storageUrl=value;}
 public Integer getPositionNo(){return positionNo;} public void setPositionNo(Integer value){this.positionNo=value;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime value){this.createdAt=value;}
}
