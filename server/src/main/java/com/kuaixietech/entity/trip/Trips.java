package com.kuaixietech.entity.trip;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.*;

/** Database schema mapped from suixinggou_merged.sql; no synthetic columns. */
@TableName("t_trips")
public class Trips {
    @TableId(value="id", type=IdType.INPUT)
    private String id;
    @TableField("owner_id")
    private String ownerId;
    @TableField("destination")
    private String destination;
    @TableField("destination_flag")
    private String destinationFlag;
    @TableField("entry_port")
    private String entryPort;
    @TableField("transport_mode")
    private String transportMode;
    @TableField("depart_date")
    private LocalDate departDate;
    @TableField("arrive_date")
    private LocalDate arriveDate;
    @TableField("return_date")
    private LocalDate returnDate;
    @TableField("capacity_qty")
    private Integer capacityQty;
    @TableField("capacity_weight_kg")
    private BigDecimal capacityWeightKg;
    @TableField("fee_mode")
    private String feeMode;
    @TableField("fee_value")
    private BigDecimal feeValue;
    @TableField("vendor_deposit_rate")
    private BigDecimal vendorDepositRate;
    @TableField("proof_urls")
    private String proofUrls;
    @TableField("verify_status")
    private String verifyStatus;
    @TableField("verify_reason")
    private String verifyReason;
    @TableField("verified_at")
    private LocalDateTime verifiedAt;
    @TableField("status")
    private String status;
    @TableField("remaining_slots")
    private Integer remainingSlots;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("flight_no")
    private String flightNo;
    @TableField("depart_datetime")
    private LocalDateTime departDatetime;
    @TableField("visa_proof_urls")
    private String visaProofUrls;
    @TableField("sms_proof_urls")
    private String smsProofUrls;
    @TableField("boarding_snapshot_url")
    private String boardingSnapshotUrl;
    @TableField("boarding_lat")
    private BigDecimal boardingLat;
    @TableField("boarding_lng")
    private BigDecimal boardingLng;
    @TableField("boarding_city")
    private String boardingCity;
    @TableField("boarding_at")
    private LocalDateTime boardingAt;
    @TableField("boarding_risk_flags")
    private String boardingRiskFlags;
    @TableField("product_ids")
    private String productIds;
    @TableField("trip_type")
    private String tripType;
    @TableField("direction")
    private String direction;
    @TableField("origin_city")
    private String originCity;
    @TableField("carry_mode")
    private String carryMode;
    @TableField("pool_slots")
    private Integer poolSlots;
    @TableField("pool_status")
    private String poolStatus;
    @TableField("ticket_proof_urls")
    private String ticketProofUrls;
    @TableField("arrival_snapshot_url")
    private String arrivalSnapshotUrl;
    @TableField("arrival_city")
    private String arrivalCity;
    @TableField("arrival_at")
    private LocalDateTime arrivalAt;
    @TableField("visibility")
    private String visibility;

    public String getId() { return id; }
    public void setId(String value) { this.id = value; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String value) { this.ownerId = value; }
    public String getDestination() { return destination; }
    public void setDestination(String value) { this.destination = value; }
    public String getDestinationFlag() { return destinationFlag; }
    public void setDestinationFlag(String value) { this.destinationFlag = value; }
    public String getEntryPort() { return entryPort; }
    public void setEntryPort(String value) { this.entryPort = value; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String value) { this.transportMode = value; }
    public LocalDate getDepartDate() { return departDate; }
    public void setDepartDate(LocalDate value) { this.departDate = value; }
    public LocalDate getArriveDate() { return arriveDate; }
    public void setArriveDate(LocalDate value) { this.arriveDate = value; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate value) { this.returnDate = value; }
    public Integer getCapacityQty() { return capacityQty; }
    public void setCapacityQty(Integer value) { this.capacityQty = value; }
    public BigDecimal getCapacityWeightKg() { return capacityWeightKg; }
    public void setCapacityWeightKg(BigDecimal value) { this.capacityWeightKg = value; }
    public String getFeeMode() { return feeMode; }
    public void setFeeMode(String value) { this.feeMode = value; }
    public BigDecimal getFeeValue() { return feeValue; }
    public void setFeeValue(BigDecimal value) { this.feeValue = value; }
    public BigDecimal getVendorDepositRate() { return vendorDepositRate; }
    public void setVendorDepositRate(BigDecimal value) { this.vendorDepositRate = value; }
    public String getProofUrls() { return proofUrls; }
    public void setProofUrls(String value) { this.proofUrls = value; }
    public String getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(String value) { this.verifyStatus = value; }
    public String getVerifyReason() { return verifyReason; }
    public void setVerifyReason(String value) { this.verifyReason = value; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime value) { this.verifiedAt = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public Integer getRemainingSlots() { return remainingSlots; }
    public void setRemainingSlots(Integer value) { this.remainingSlots = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String value) { this.flightNo = value; }
    public LocalDateTime getDepartDatetime() { return departDatetime; }
    public void setDepartDatetime(LocalDateTime value) { this.departDatetime = value; }
    public String getVisaProofUrls() { return visaProofUrls; }
    public void setVisaProofUrls(String value) { this.visaProofUrls = value; }
    public String getSmsProofUrls() { return smsProofUrls; }
    public void setSmsProofUrls(String value) { this.smsProofUrls = value; }
    public String getBoardingSnapshotUrl() { return boardingSnapshotUrl; }
    public void setBoardingSnapshotUrl(String value) { this.boardingSnapshotUrl = value; }
    public BigDecimal getBoardingLat() { return boardingLat; }
    public void setBoardingLat(BigDecimal value) { this.boardingLat = value; }
    public BigDecimal getBoardingLng() { return boardingLng; }
    public void setBoardingLng(BigDecimal value) { this.boardingLng = value; }
    public String getBoardingCity() { return boardingCity; }
    public void setBoardingCity(String value) { this.boardingCity = value; }
    public LocalDateTime getBoardingAt() { return boardingAt; }
    public void setBoardingAt(LocalDateTime value) { this.boardingAt = value; }
    public String getBoardingRiskFlags() { return boardingRiskFlags; }
    public void setBoardingRiskFlags(String value) { this.boardingRiskFlags = value; }
    public String getProductIds() { return productIds; }
    public void setProductIds(String value) { this.productIds = value; }
    public String getTripType() { return tripType; }
    public void setTripType(String value) { this.tripType = value; }
    public String getDirection() { return direction; }
    public void setDirection(String value) { this.direction = value; }
    public String getOriginCity() { return originCity; }
    public void setOriginCity(String value) { this.originCity = value; }
    public String getCarryMode() { return carryMode; }
    public void setCarryMode(String value) { this.carryMode = value; }
    public Integer getPoolSlots() { return poolSlots; }
    public void setPoolSlots(Integer value) { this.poolSlots = value; }
    public String getPoolStatus() { return poolStatus; }
    public void setPoolStatus(String value) { this.poolStatus = value; }
    public String getTicketProofUrls() { return ticketProofUrls; }
    public void setTicketProofUrls(String value) { this.ticketProofUrls = value; }
    public String getArrivalSnapshotUrl() { return arrivalSnapshotUrl; }
    public void setArrivalSnapshotUrl(String value) { this.arrivalSnapshotUrl = value; }
    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String value) { this.arrivalCity = value; }
    public LocalDateTime getArrivalAt() { return arrivalAt; }
    public void setArrivalAt(LocalDateTime value) { this.arrivalAt = value; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String value) { this.visibility = value; }
}
