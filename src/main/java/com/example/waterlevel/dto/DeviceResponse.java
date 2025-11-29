package com.example.waterlevel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO for device information responses. */
public class DeviceResponse {

  private Long id;
  private String name;
  private String deviceKey;
  private BigDecimal minThreshold;
  private BigDecimal maxThreshold;
  private Long adminId;
  private String adminUsername;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public DeviceResponse() {}

  public DeviceResponse(
      final Long id,
      final String name,
      final String deviceKey,
      final BigDecimal minThreshold,
      final BigDecimal maxThreshold,
      final Long adminId,
      final String adminUsername,
      final LocalDateTime createdAt,
      final LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.deviceKey = deviceKey;
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
    this.adminId = adminId;
    this.adminUsername = adminUsername;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDeviceKey() {
    return deviceKey;
  }

  public void setDeviceKey(final String deviceKey) {
    this.deviceKey = deviceKey;
  }

  public BigDecimal getMinThreshold() {
    return minThreshold;
  }

  public void setMinThreshold(final BigDecimal minThreshold) {
    this.minThreshold = minThreshold;
  }

  public BigDecimal getMaxThreshold() {
    return maxThreshold;
  }

  public void setMaxThreshold(final BigDecimal maxThreshold) {
    this.maxThreshold = maxThreshold;
  }

  public Long getAdminId() {
    return adminId;
  }

  public void setAdminId(final Long adminId) {
    this.adminId = adminId;
  }

  public String getAdminUsername() {
    return adminUsername;
  }

  public void setAdminUsername(final String adminUsername) {
    this.adminUsername = adminUsername;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(final LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
