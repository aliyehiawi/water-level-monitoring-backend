package com.example.waterlevel.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for device registration request. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

  /**
   * Validates that minimum threshold is less than maximum threshold.
   *
   * @return true if minThreshold < maxThreshold
   */
  @AssertTrue(message = "Minimum threshold must be less than maximum threshold")
  @JsonIgnore // Prevent Jackson from trying to deserialize this validation method
  public boolean isValidThresholdRange() {
    return minThreshold != null && maxThreshold != null && minThreshold < maxThreshold;
  }
}
