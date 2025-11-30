package com.example.waterlevel.dto.websocket;

import com.example.waterlevel.entity.PumpStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for sensor update WebSocket message. */
@Getter
@Setter
@NoArgsConstructor
public class SensorUpdateMessage extends BaseMessage {
  private Double waterLevel;
  private PumpStatus pumpStatus;

  /**
   * Creates a sensor update message.
   *
   * @param type the message type
   * @param deviceId the device ID
   * @param waterLevel the current water level
   * @param pumpStatus the current pump status
   * @param timestamp the timestamp of the reading
   */
  public SensorUpdateMessage(
      final String type,
      final Long deviceId,
      final Double waterLevel,
      final PumpStatus pumpStatus,
      final String timestamp) {
    super(type, deviceId, timestamp);
    this.waterLevel = waterLevel;
    this.pumpStatus = pumpStatus;
  }
}
