package com.example.waterlevel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for device information responses. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("java:S107") // Lombok generates constructor, 9 parameters acceptable for DTO
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
}
