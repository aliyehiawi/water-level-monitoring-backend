package com.example.waterlevel.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for threshold information responses. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdResponse {

  private BigDecimal minThreshold;
  private BigDecimal maxThreshold;
}
