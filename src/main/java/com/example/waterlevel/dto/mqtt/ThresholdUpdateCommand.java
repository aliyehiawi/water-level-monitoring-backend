package com.example.waterlevel.dto.mqtt;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for MQTT threshold update command. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdUpdateCommand {
  private Double minThreshold;
  private Double maxThreshold;
  private String timestamp;
  private Long updatedBy;

  /**
   * Creates a threshold update command.
   *
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   * @param timestamp the timestamp when update was made
   * @param updatedBy the user ID who updated the thresholds
   */
  public ThresholdUpdateCommand(
      final Double minThreshold,
      final Double maxThreshold,
      final Instant timestamp,
      final Long updatedBy) {
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
    this.timestamp = timestamp.toString();
    this.updatedBy = updatedBy;
  }
}
