package com.example.waterlevel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO for device registration request. */
public class DeviceRegisterRequest {

  @NotBlank(message = "Device name is required")
  @Size(min = 1, max = 100, message = "Device name must be between 1 and 100 characters")
  private String name;

  @NotNull(message = "Minimum threshold is required")
  @DecimalMin(value = "0.0", message = "Minimum threshold must be >= 0")
  @DecimalMax(value = "999.99", message = "Minimum threshold must be <= 999.99")
  private Double minThreshold;

  @NotNull(message = "Maximum threshold is required")
  @DecimalMin(value = "0.0", message = "Maximum threshold must be >= 0")
  @DecimalMax(value = "999.99", message = "Maximum threshold must be <= 999.99")
  private Double maxThreshold;

  public DeviceRegisterRequest() {}

  public DeviceRegisterRequest(
      final String name, final Double minThreshold, final Double maxThreshold) {
    this.name = name;
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Double getMinThreshold() {
    return minThreshold;
  }

  public void setMinThreshold(final Double minThreshold) {
    this.minThreshold = minThreshold;
  }

  public Double getMaxThreshold() {
    return maxThreshold;
  }

  public void setMaxThreshold(final Double maxThreshold) {
    this.maxThreshold = maxThreshold;
  }
}
