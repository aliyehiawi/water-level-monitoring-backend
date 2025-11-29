package com.example.waterlevel.dto;

import java.math.BigDecimal;

/** DTO for threshold information responses. */
public class ThresholdResponse {

  private BigDecimal minThreshold;
  private BigDecimal maxThreshold;

  public ThresholdResponse() {}

  public ThresholdResponse(final BigDecimal minThreshold, final BigDecimal maxThreshold) {
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
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
}
