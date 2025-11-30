package com.example.waterlevel.dto;

import com.example.waterlevel.entity.PumpStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for pump status responses. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PumpStatusResponse {

  private PumpStatus pumpStatus;
  private LocalDateTime lastUpdate;
}
