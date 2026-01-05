package com.example.waterlevel.dto.websocket;

import com.example.waterlevel.entity.PumpStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PumpStatusMessage extends BaseMessage {
  private PumpStatus pumpStatus;

  /**
   * Creates a pump status message.
   *
   * @param type the message type
   * @param deviceId the device ID
   * @param pumpStatus the new pump status
   * @param timestamp the timestamp
   */
  public PumpStatusMessage(
      final String type, final Long deviceId, final PumpStatus pumpStatus, final String timestamp) {
    super(type, deviceId, timestamp);
    this.pumpStatus = pumpStatus;
  }
}
