package com.example.waterlevel.dto;

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

  private String pumpStatus;
  private LocalDateTime lastUpdate;
}
