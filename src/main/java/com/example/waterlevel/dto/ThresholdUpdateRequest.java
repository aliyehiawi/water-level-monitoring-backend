package com.example.waterlevel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** DTO for threshold update request. */
public class ThresholdUpdateRequest {

  @NotNull(message = "Minimum threshold is required")
  @DecimalMin(value = "0.0", message = "Minimum threshold must be >= 0")
  @DecimalMax(value = "999.99", message = "Minimum threshold must be <= 999.99")
  private Double minThreshold;

  @NotNull(message = "Maximum threshold is required")
  @DecimalMin(value = "0.0", message = "Maximum threshold must be >= 0")
  @DecimalMax(value = "999.99", message = "Maximum threshold must be <= 999.99")
  private Double maxThreshold;

  public ThresholdUpdateRequest() {}

  public ThresholdUpdateRequest(final Double minThreshold, final Double maxThreshold) {
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
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
