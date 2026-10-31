package com.kuaixietech.entity.order_fulfillment;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_orders")
public class Orders {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("order_no")
    private String orderNo;
    @TableField("trip_id")
    private String tripId;
    @TableField("buyer_id")
    private String buyerId;
    @TableField("vendor_id")
    private String vendorId;
    @TableField("demand_id")
    private String demandId;
    @TableField("source")
    private String source;
    @TableField("item_name")
    private String itemName;
    @TableField("item_spec")
    private String itemSpec;
    @TableField("quantity")
    private Integer quantity;
    @TableField("unit")
    private String unit;
    @TableField("expect_unit_price")
    private BigDecimal expectUnitPrice;
    @TableField("goods_amount")
    private BigDecimal goodsAmount;
    @TableField("service_fee")
    private BigDecimal serviceFee;
    @TableField("buyer_deposit")
    private BigDecimal buyerDeposit;
    @TableField("vendor_deposit")
    private BigDecimal vendorDeposit;
    @TableField("final_price")
    private BigDecimal finalPrice;
    @TableField("ref_image_urls")
    private String refImageUrls;
    @TableField("expect_arrive_date")
    private LocalDate expectArriveDate;
    @TableField("promised_arrive_date")
    private LocalDate promisedArriveDate;
    @TableField("contact_name")
    private String contactName;
    @TableField("contact_phone")
    private String contactPhone;
    @TableField("address")
    private String address;
    @TableField("note")
    private String note;
    @TableField("status")
    private String status;
    @TableField("buyer_deposit_status")
    private String buyerDepositStatus;
    @TableField("vendor_deposit_status")
    private String vendorDepositStatus;
    @TableField("negotiation_quote")
    private BigDecimal negotiationQuote;
    @TableField("negotiation_note")
    private String negotiationNote;
    @TableField("negotiation_deadline")
    private LocalDateTime negotiationDeadline;
    @TableField("intent_deadline")
    private LocalDateTime intentDeadline;
    @TableField("vendor_deposit_deadline")
    private LocalDateTime vendorDepositDeadline;
    @TableField("purchase_receipt_urls")
    private String purchaseReceiptUrls;
    @TableField("carrier_code")
    private String carrierCode;
    @TableField("tracking_no")
    private String trackingNo;
    @TableField("shipped_at")
    private LocalDateTime shippedAt;
    @TableField("auto_confirm_at")
    private LocalDateTime autoConfirmAt;
    @TableField("agreed_json")
    private String agreedJson;
    @TableField("cancel_reason_type")
    private String cancelReasonType;
    @TableField("cancel_force_majeure")
    private Boolean cancelForceMajeure;
    @TableField("cancel_proof_urls")
    private String cancelProofUrls;
    @TableField("cancel_penalty_amount")
    private BigDecimal cancelPenaltyAmount;
    @TableField("cancel_withdraw_used")
    private Boolean cancelWithdrawUsed;
    @TableField("closed_reason")
    private String closedReason;
    @TableField("settled_at")
    private LocalDateTime settledAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("support_state")
    private String supportState;
    @TableField("support_ticket_id")
    private String supportTicketId;
    @TableField("purchase_evidence")
    private String purchaseEvidence;
    @TableField("procurement_lat")
    private BigDecimal procurementLat;
    @TableField("procurement_lng")
    private BigDecimal procurementLng;
    @TableField("procurement_address")
    private String procurementAddress;
    @TableField("procurement_city")
    private String procurementCity;
    @TableField("procurement_at")
    private LocalDateTime procurementAt;
    @TableField("purchase_face_verification_id")
    private String purchaseFaceVerificationId;
    @TableField("location_mismatch")
    private Boolean locationMismatch;
    @TableField("shipping_fee")
    private BigDecimal shippingFee;
    @TableField("shipping_fee_note")
    private String shippingFeeNote;
    @TableField("shipping_fee_status")
    private String shippingFeeStatus;
    @TableField("shipping_fee_deadline")
    private LocalDateTime shippingFeeDeadline;
    @TableField("freight_label_urls")
    private String freightLabelUrls;
    @TableField("ship_lat")
    private BigDecimal shipLat;
    @TableField("ship_lng")
    private BigDecimal shipLng;
    @TableField("ship_city")
    private String shipCity;
    @TableField("ship_address")
    private String shipAddress;
    @TableField("ship_risk_flags")
    private String shipRiskFlags;
    @TableField("purchase_meta")
    private String purchaseMeta;
    @TableField("purchase_risk_flags")
    private String purchaseRiskFlags;
    @TableField("confirmed_at")
    private LocalDateTime confirmedAt;
    @TableField("cancel_tier")
    private String cancelTier;
    @TableField("pool_slot_no")
    private Integer poolSlotNo;
    @TableField("tax_refund_plan")
    private String taxRefundPlan;
    @TableField("tax_rate")
    private BigDecimal taxRate;
    @TableField("tax_refund_amount")
    private BigDecimal taxRefundAmount;
    @TableField("delivery_mode")
    private String deliveryMode;
    @TableField("meetup_code")
    private String meetupCode;
    @TableField("meetup_confirmed_json")
    private String meetupConfirmedJson;
    @TableField("insured")
    private String insured;
    @TableField("insurance_premium")
    private BigDecimal insurancePremium;
    @TableField("declared_value")
    private BigDecimal declaredValue;
    @TableField("delivery_face_verification_id")
    private String deliveryFaceVerificationId;
    @TableField("delivery_lat")
    private BigDecimal deliveryLat;
    @TableField("delivery_lng")
    private BigDecimal deliveryLng;
    @TableField("delivery_city")
    private String deliveryCity;
    @TableField("delivery_at")
    private LocalDateTime deliveryAt;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { this.orderNo = value; }
    public String getTripId() { return tripId; }
    public void setTripId(String value) { this.tripId = value; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String value) { this.buyerId = value; }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String value) { this.vendorId = value; }
    public String getDemandId() { return demandId; }
    public void setDemandId(String value) { this.demandId = value; }
    public String getSource() { return source; }
    public void setSource(String value) { this.source = value; }
    public String getItemName() { return itemName; }
    public void setItemName(String value) { this.itemName = value; }
    public String getItemSpec() { return itemSpec; }
    public void setItemSpec(String value) { this.itemSpec = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { this.quantity = value; }
    public String getUnit() { return unit; }
    public void setUnit(String value) { this.unit = value; }
    public BigDecimal getExpectUnitPrice() { return expectUnitPrice; }
    public void setExpectUnitPrice(BigDecimal value) { this.expectUnitPrice = value; }
    public BigDecimal getGoodsAmount() { return goodsAmount; }
    public void setGoodsAmount(BigDecimal value) { this.goodsAmount = value; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal value) { this.serviceFee = value; }
    public BigDecimal getBuyerDeposit() { return buyerDeposit; }
    public void setBuyerDeposit(BigDecimal value) { this.buyerDeposit = value; }
    public BigDecimal getVendorDeposit() { return vendorDeposit; }
    public void setVendorDeposit(BigDecimal value) { this.vendorDeposit = value; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal value) { this.finalPrice = value; }
    public String getRefImageUrls() { return refImageUrls; }
    public void setRefImageUrls(String value) { this.refImageUrls = value; }
    public LocalDate getExpectArriveDate() { return expectArriveDate; }
    public void setExpectArriveDate(LocalDate value) { this.expectArriveDate = value; }
    public LocalDate getPromisedArriveDate() { return promisedArriveDate; }
    public void setPromisedArriveDate(LocalDate value) { this.promisedArriveDate = value; }
    public String getContactName() { return contactName; }
    public void setContactName(String value) { this.contactName = value; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String value) { this.contactPhone = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getBuyerDepositStatus() { return buyerDepositStatus; }
    public void setBuyerDepositStatus(String value) { this.buyerDepositStatus = value; }
    public String getVendorDepositStatus() { return vendorDepositStatus; }
    public void setVendorDepositStatus(String value) { this.vendorDepositStatus = value; }
    public BigDecimal getNegotiationQuote() { return negotiationQuote; }
    public void setNegotiationQuote(BigDecimal value) { this.negotiationQuote = value; }
    public String getNegotiationNote() { return negotiationNote; }
    public void setNegotiationNote(String value) { this.negotiationNote = value; }
    public LocalDateTime getNegotiationDeadline() { return negotiationDeadline; }
    public void setNegotiationDeadline(LocalDateTime value) { this.negotiationDeadline = value; }
    public LocalDateTime getIntentDeadline() { return intentDeadline; }
    public void setIntentDeadline(LocalDateTime value) { this.intentDeadline = value; }
    public LocalDateTime getVendorDepositDeadline() { return vendorDepositDeadline; }
    public void setVendorDepositDeadline(LocalDateTime value) { this.vendorDepositDeadline = value; }
    public String getPurchaseReceiptUrls() { return purchaseReceiptUrls; }
    public void setPurchaseReceiptUrls(String value) { this.purchaseReceiptUrls = value; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String value) { this.carrierCode = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { this.trackingNo = value; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime value) { this.shippedAt = value; }
    public LocalDateTime getAutoConfirmAt() { return autoConfirmAt; }
    public void setAutoConfirmAt(LocalDateTime value) { this.autoConfirmAt = value; }
    public String getAgreedJson() { return agreedJson; }
    public void setAgreedJson(String value) { this.agreedJson = value; }
    public String getCancelReasonType() { return cancelReasonType; }
    public void setCancelReasonType(String value) { this.cancelReasonType = value; }
    public Boolean getCancelForceMajeure() { return cancelForceMajeure; }
    public void setCancelForceMajeure(Boolean value) { this.cancelForceMajeure = value; }
    public String getCancelProofUrls() { return cancelProofUrls; }
    public void setCancelProofUrls(String value) { this.cancelProofUrls = value; }
    public BigDecimal getCancelPenaltyAmount() { return cancelPenaltyAmount; }
    public void setCancelPenaltyAmount(BigDecimal value) { this.cancelPenaltyAmount = value; }
    public Boolean getCancelWithdrawUsed() { return cancelWithdrawUsed; }
    public void setCancelWithdrawUsed(Boolean value) { this.cancelWithdrawUsed = value; }
    public String getClosedReason() { return closedReason; }
    public void setClosedReason(String value) { this.closedReason = value; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime value) { this.settledAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
    public String getSupportState() { return supportState; }
    public void setSupportState(String value) { this.supportState = value; }
    public String getSupportTicketId() { return supportTicketId; }
    public void setSupportTicketId(String value) { this.supportTicketId = value; }
    public String getPurchaseEvidence() { return purchaseEvidence; }
    public void setPurchaseEvidence(String value) { this.purchaseEvidence = value; }
    public BigDecimal getProcurementLat() { return procurementLat; }
    public void setProcurementLat(BigDecimal value) { this.procurementLat = value; }
    public BigDecimal getProcurementLng() { return procurementLng; }
    public void setProcurementLng(BigDecimal value) { this.procurementLng = value; }
    public String getProcurementAddress() { return procurementAddress; }
    public void setProcurementAddress(String value) { this.procurementAddress = value; }
    public String getProcurementCity() { return procurementCity; }
    public void setProcurementCity(String value) { this.procurementCity = value; }
    public LocalDateTime getProcurementAt() { return procurementAt; }
    public void setProcurementAt(LocalDateTime value) { this.procurementAt = value; }
    public String getPurchaseFaceVerificationId() { return purchaseFaceVerificationId; }
    public void setPurchaseFaceVerificationId(String value) { this.purchaseFaceVerificationId = value; }
    public Boolean getLocationMismatch() { return locationMismatch; }
    public void setLocationMismatch(Boolean value) { this.locationMismatch = value; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal value) { this.shippingFee = value; }
    public String getShippingFeeNote() { return shippingFeeNote; }
    public void setShippingFeeNote(String value) { this.shippingFeeNote = value; }
    public String getShippingFeeStatus() { return shippingFeeStatus; }
    public void setShippingFeeStatus(String value) { this.shippingFeeStatus = value; }
    public LocalDateTime getShippingFeeDeadline() { return shippingFeeDeadline; }
    public void setShippingFeeDeadline(LocalDateTime value) { this.shippingFeeDeadline = value; }
    public String getFreightLabelUrls() { return freightLabelUrls; }
    public void setFreightLabelUrls(String value) { this.freightLabelUrls = value; }
    public BigDecimal getShipLat() { return shipLat; }
    public void setShipLat(BigDecimal value) { this.shipLat = value; }
    public BigDecimal getShipLng() { return shipLng; }
    public void setShipLng(BigDecimal value) { this.shipLng = value; }
    public String getShipCity() { return shipCity; }
    public void setShipCity(String value) { this.shipCity = value; }
    public String getShipAddress() { return shipAddress; }
    public void setShipAddress(String value) { this.shipAddress = value; }
    public String getShipRiskFlags() { return shipRiskFlags; }
    public void setShipRiskFlags(String value) { this.shipRiskFlags = value; }
    public String getPurchaseMeta() { return purchaseMeta; }
    public void setPurchaseMeta(String value) { this.purchaseMeta = value; }
    public String getPurchaseRiskFlags() { return purchaseRiskFlags; }
    public void setPurchaseRiskFlags(String value) { this.purchaseRiskFlags = value; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime value) { this.confirmedAt = value; }
    public String getCancelTier() { return cancelTier; }
    public void setCancelTier(String value) { this.cancelTier = value; }
    public Integer getPoolSlotNo() { return poolSlotNo; }
    public void setPoolSlotNo(Integer value) { this.poolSlotNo = value; }
    public String getTaxRefundPlan() { return taxRefundPlan; }
    public void setTaxRefundPlan(String value) { this.taxRefundPlan = value; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal value) { this.taxRate = value; }
    public BigDecimal getTaxRefundAmount() { return taxRefundAmount; }
    public void setTaxRefundAmount(BigDecimal value) { this.taxRefundAmount = value; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String value) { this.deliveryMode = value; }
    public String getMeetupCode() { return meetupCode; }
    public void setMeetupCode(String value) { this.meetupCode = value; }
    public String getMeetupConfirmedJson() { return meetupConfirmedJson; }
    public void setMeetupConfirmedJson(String value) { this.meetupConfirmedJson = value; }
    public String getInsured() { return insured; }
    public void setInsured(String value) { this.insured = value; }
    public BigDecimal getInsurancePremium() { return insurancePremium; }
    public void setInsurancePremium(BigDecimal value) { this.insurancePremium = value; }
    public BigDecimal getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(BigDecimal value) { this.declaredValue = value; }
    public String getDeliveryFaceVerificationId() { return deliveryFaceVerificationId; }
    public void setDeliveryFaceVerificationId(String value) { this.deliveryFaceVerificationId = value; }
    public BigDecimal getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(BigDecimal value) { this.deliveryLat = value; }
    public BigDecimal getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(BigDecimal value) { this.deliveryLng = value; }
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String value) { this.deliveryCity = value; }
    public LocalDateTime getDeliveryAt() { return deliveryAt; }
    public void setDeliveryAt(LocalDateTime value) { this.deliveryAt = value; }
}
