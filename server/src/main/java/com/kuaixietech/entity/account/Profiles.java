package com.kuaixietech.entity.account;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_profiles")
public class Profiles {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("username")
    private String username;
    @TableField("openid")
    private String openid;
    @TableField("unionid")
    private String unionid;
    @TableField("nickname")
    private String nickname;
    @TableField("avatar_url")
    private String avatarUrl;
    @TableField("phone")
    private String phone;
    @TableField("city")
    private String city;
    @TableField("is_admin")
    private Boolean isAdmin;
    @TableField("invite_code_used")
    private String inviteCodeUsed;
    @TableField("carrier_verify_status")
    private String carrierVerifyStatus;
    @TableField("carrier_verify_reason")
    private String carrierVerifyReason;
    @TableField("verified_at")
    private LocalDateTime verifiedAt;
    @TableField("performance_score")
    private Integer performanceScore;
    @TableField("star_avg")
    private BigDecimal starAvg;
    @TableField("star_count")
    private Integer starCount;
    @TableField("deal_count")
    private Integer dealCount;
    @TableField("ontime_count")
    private Integer ontimeCount;
    @TableField("breach_count_90d")
    private Integer breachCount90D;
    @TableField("abandon_count_90d")
    private Integer abandonCount90D;
    @TableField("lost_dispute_count_90d")
    private Integer lostDisputeCount90D;
    @TableField("clean_streak")
    private Integer cleanStreak;
    @TableField("dispute_reject_streak")
    private Integer disputeRejectStreak;
    @TableField("banned_until")
    private LocalDateTime bannedUntil;
    @TableField("boost_factor")
    private BigDecimal boostFactor;
    @TableField("balance")
    private BigDecimal balance;
    @TableField("payout_account")
    private String payoutAccount;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("my_invite_code")
    private String myInviteCode;
    @TableField("invited_by")
    private String invitedBy;
    @TableField("points")
    private Integer points;
    @TableField("real_name")
    private String realName;
    @TableField("id_card_no_masked")
    private String idCardNoMasked;
    @TableField("passport_no_masked")
    private String passportNoMasked;
    @TableField("email")
    private String email;
    @TableField("phone_verified")
    private Boolean phoneVerified;
    @TableField("email_verified")
    private Boolean emailVerified;
    @TableField("user_role")
    private String userRole;
    @TableField("buyer_verify_status")
    private String buyerVerifyStatus;
    @TableField("buyer_verified_at")
    private LocalDateTime buyerVerifiedAt;
    @TableField("carrier_tier")
    private String carrierTier;
    @TableField("buyer_verify_reason")
    private String buyerVerifyReason;
    @TableField("invite_tier")
    private String inviteTier;
    @TableField("valid_invitee_count")
    private Integer validInviteeCount;
    @TableField("commission_mtd")
    private BigDecimal commissionMtd;
    @TableField("commission_mtd_month")
    private String commissionMtdMonth;
    @TableField("gender")
    private String gender;
    @TableField("notify_order")
    private Boolean notifyOrder;
    @TableField("notify_logistics")
    private Boolean notifyLogistics;
    @TableField("notify_commission")
    private Boolean notifyCommission;
    @TableField("notify_system")
    private Boolean notifySystem;
    @TableField("consent_location")
    private Boolean consentLocation;
    @TableField("consent_camera")
    private Boolean consentCamera;
    @TableField("consent_album")
    private Boolean consentAlbum;
    @TableField("personalized_reco")
    private Boolean personalizedReco;
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
    @TableField("consent_audio")
    private Boolean consentAudio;
    @TableField("enterprise_name")
    private String enterpriseName;
    @TableField("business_license_no")
    private String businessLicenseNo;
    @TableField("enterprise_verify_status")
    private String enterpriseVerifyStatus;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getUsername() { return username; }
    public void setUsername(String value) { this.username = value; }
    public String getOpenid() { return openid; }
    public void setOpenid(String value) { this.openid = value; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String value) { this.unionid = value; }
    public String getNickname() { return nickname; }
    public void setNickname(String value) { this.nickname = value; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String value) { this.avatarUrl = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }
    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean value) { this.isAdmin = value; }
    public String getInviteCodeUsed() { return inviteCodeUsed; }
    public void setInviteCodeUsed(String value) { this.inviteCodeUsed = value; }
    public String getCarrierVerifyStatus() { return carrierVerifyStatus; }
    public void setCarrierVerifyStatus(String value) { this.carrierVerifyStatus = value; }
    public String getCarrierVerifyReason() { return carrierVerifyReason; }
    public void setCarrierVerifyReason(String value) { this.carrierVerifyReason = value; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime value) { this.verifiedAt = value; }
    public Integer getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Integer value) { this.performanceScore = value; }
    public BigDecimal getStarAvg() { return starAvg; }
    public void setStarAvg(BigDecimal value) { this.starAvg = value; }
    public Integer getStarCount() { return starCount; }
    public void setStarCount(Integer value) { this.starCount = value; }
    public Integer getDealCount() { return dealCount; }
    public void setDealCount(Integer value) { this.dealCount = value; }
    public Integer getOntimeCount() { return ontimeCount; }
    public void setOntimeCount(Integer value) { this.ontimeCount = value; }
    public Integer getBreachCount90D() { return breachCount90D; }
    public void setBreachCount90D(Integer value) { this.breachCount90D = value; }
    public Integer getAbandonCount90D() { return abandonCount90D; }
    public void setAbandonCount90D(Integer value) { this.abandonCount90D = value; }
    public Integer getLostDisputeCount90D() { return lostDisputeCount90D; }
    public void setLostDisputeCount90D(Integer value) { this.lostDisputeCount90D = value; }
    public Integer getCleanStreak() { return cleanStreak; }
    public void setCleanStreak(Integer value) { this.cleanStreak = value; }
    public Integer getDisputeRejectStreak() { return disputeRejectStreak; }
    public void setDisputeRejectStreak(Integer value) { this.disputeRejectStreak = value; }
    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime value) { this.bannedUntil = value; }
    public BigDecimal getBoostFactor() { return boostFactor; }
    public void setBoostFactor(BigDecimal value) { this.boostFactor = value; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal value) { this.balance = value; }
    public String getPayoutAccount() { return payoutAccount; }
    public void setPayoutAccount(String value) { this.payoutAccount = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public String getMyInviteCode() { return myInviteCode; }
    public void setMyInviteCode(String value) { this.myInviteCode = value; }
    public String getInvitedBy() { return invitedBy; }
    public void setInvitedBy(String value) { this.invitedBy = value; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer value) { this.points = value; }
    public String getRealName() { return realName; }
    public void setRealName(String value) { this.realName = value; }
    public String getIdCardNoMasked() { return idCardNoMasked; }
    public void setIdCardNoMasked(String value) { this.idCardNoMasked = value; }
    public String getPassportNoMasked() { return passportNoMasked; }
    public void setPassportNoMasked(String value) { this.passportNoMasked = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean value) { this.phoneVerified = value; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean value) { this.emailVerified = value; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String value) { this.userRole = value; }
    public String getBuyerVerifyStatus() { return buyerVerifyStatus; }
    public void setBuyerVerifyStatus(String value) { this.buyerVerifyStatus = value; }
    public LocalDateTime getBuyerVerifiedAt() { return buyerVerifiedAt; }
    public void setBuyerVerifiedAt(LocalDateTime value) { this.buyerVerifiedAt = value; }
    public String getCarrierTier() { return carrierTier; }
    public void setCarrierTier(String value) { this.carrierTier = value; }
    public String getBuyerVerifyReason() { return buyerVerifyReason; }
    public void setBuyerVerifyReason(String value) { this.buyerVerifyReason = value; }
    public String getInviteTier() { return inviteTier; }
    public void setInviteTier(String value) { this.inviteTier = value; }
    public Integer getValidInviteeCount() { return validInviteeCount; }
    public void setValidInviteeCount(Integer value) { this.validInviteeCount = value; }
    public BigDecimal getCommissionMtd() { return commissionMtd; }
    public void setCommissionMtd(BigDecimal value) { this.commissionMtd = value; }
    public String getCommissionMtdMonth() { return commissionMtdMonth; }
    public void setCommissionMtdMonth(String value) { this.commissionMtdMonth = value; }
    public String getGender() { return gender; }
    public void setGender(String value) { this.gender = value; }
    public Boolean getNotifyOrder() { return notifyOrder; }
    public void setNotifyOrder(Boolean value) { this.notifyOrder = value; }
    public Boolean getNotifyLogistics() { return notifyLogistics; }
    public void setNotifyLogistics(Boolean value) { this.notifyLogistics = value; }
    public Boolean getNotifyCommission() { return notifyCommission; }
    public void setNotifyCommission(Boolean value) { this.notifyCommission = value; }
    public Boolean getNotifySystem() { return notifySystem; }
    public void setNotifySystem(Boolean value) { this.notifySystem = value; }
    public Boolean getConsentLocation() { return consentLocation; }
    public void setConsentLocation(Boolean value) { this.consentLocation = value; }
    public Boolean getConsentCamera() { return consentCamera; }
    public void setConsentCamera(Boolean value) { this.consentCamera = value; }
    public Boolean getConsentAlbum() { return consentAlbum; }
    public void setConsentAlbum(Boolean value) { this.consentAlbum = value; }
    public Boolean getPersonalizedReco() { return personalizedReco; }
    public void setPersonalizedReco(Boolean value) { this.personalizedReco = value; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime value) { this.deletedAt = value; }
    public Boolean getConsentAudio() { return consentAudio; }
    public void setConsentAudio(Boolean value) { this.consentAudio = value; }
    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String value) { this.enterpriseName = value; }
    public String getBusinessLicenseNo() { return businessLicenseNo; }
    public void setBusinessLicenseNo(String value) { this.businessLicenseNo = value; }
    public String getEnterpriseVerifyStatus() { return enterpriseVerifyStatus; }
    public void setEnterpriseVerifyStatus(String value) { this.enterpriseVerifyStatus = value; }
}
