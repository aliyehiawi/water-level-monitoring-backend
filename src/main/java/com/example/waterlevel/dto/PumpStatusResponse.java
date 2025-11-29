package com.example.waterlevel.dto;

import java.time.LocalDateTime;

/** DTO for pump status responses. */
public class PumpStatusResponse {

  private String pumpStatus;
  private LocalDateTime lastUpdate;

  public PumpStatusResponse() {}

  public PumpStatusResponse(final String pumpStatus, final LocalDateTime lastUpdate) {
    this.pumpStatus = pumpStatus;
    this.lastUpdate = lastUpdate;
  }

  public String getPumpStatus() {
    return pumpStatus;
  }

  public void setPumpStatus(final String pumpStatus) {
    this.pumpStatus = pumpStatus;
  }

  public LocalDateTime getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(final LocalDateTime lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
