package com.example.waterlevel.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for threshold update WebSocket message. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdUpdateMessage extends BaseMessage {
  private Double minThreshold;
  private Double maxThreshold;

  /**
   * Creates a threshold update message.
   *
   * @param type the message type
   * @param deviceId the device ID
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   */
  public ThresholdUpdateMessage(
      final String type,
      final Long deviceId,
      final Double minThreshold,
      final Double maxThreshold) {
    super(type, deviceId, null);
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
  }
}
