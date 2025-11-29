package com.example.waterlevel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for threshold update request. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdUpdateRequest {

  @NotNull(message = "Minimum threshold is required")
  @DecimalMin(value = "0.0", message = "Minimum threshold must be >= 0")
  @DecimalMax(value = "999.99", message = "Minimum threshold must be <= 999.99")
  private Double minThreshold;

  @NotNull(message = "Maximum threshold is required")
  @DecimalMin(value = "0.0", message = "Maximum threshold must be >= 0")
  @DecimalMax(value = "999.99", message = "Maximum threshold must be <= 999.99")
  private Double maxThreshold;
}
