package com.example.waterlevel.dto;

import com.example.waterlevel.entity.PumpStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaterLevelDataResponse {
  private final Long id;
  private final Long deviceId;
  private final BigDecimal waterLevel;
  private final PumpStatus pumpStatus;
  private final LocalDateTime timestamp;
}
