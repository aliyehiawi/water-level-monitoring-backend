package com.example.waterlevel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time updates to frontend via WebSocket.
 *
 * <p>Broadcasts sensor data updates and device status changes to connected WebSocket clients.
 */
@Service
public class WebSocketService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketService.class);

  private final SimpMessagingTemplate messagingTemplate;

  @Autowired
  public WebSocketService(final SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Sends sensor data update to frontend.
   *
   * @param deviceId the device ID
   * @param waterLevel the current water level
   * @param pumpStatus the current pump status
   * @param timestamp the timestamp of the reading
   */
  public void sendSensorUpdate(
      final Long deviceId,
      final Double waterLevel,
      final String pumpStatus,
      final String timestamp) {
    try {
      SensorUpdateMessage message =
          new SensorUpdateMessage("sensor_update", deviceId, waterLevel, pumpStatus, timestamp);

      String destination = "/topic/device/" + deviceId;
      messagingTemplate.convertAndSend(destination, message);

      LOGGER.debug("WebSocket message sent to {}: {}", destination, message);
    } catch (Exception e) {
      LOGGER.error("Failed to send WebSocket sensor update for device {}", deviceId, e);
    }
  }

  /**
   * Sends pump status change notification to frontend.
   *
   * @param deviceId the device ID
   * @param pumpStatus the new pump status
   * @param timestamp the timestamp
   */
  public void sendPumpStatusUpdate(
      final Long deviceId, final String pumpStatus, final String timestamp) {
    try {
      PumpStatusMessage message =
          new PumpStatusMessage("pump_status", deviceId, pumpStatus, timestamp);

      String destination = "/topic/device/" + deviceId;
      messagingTemplate.convertAndSend(destination, message);

      LOGGER.debug("WebSocket pump status update sent to {}: {}", destination, message);
    } catch (Exception e) {
      LOGGER.error("Failed to send WebSocket pump status update for device {}", deviceId, e);
    }
  }

  /** DTO for sensor update message. */
  public static class SensorUpdateMessage {
    private String type;
    private Long deviceId;
    private Double waterLevel;
    private String pumpStatus;
    private String timestamp;

    public SensorUpdateMessage(
        final String type,
        final Long deviceId,
        final Double waterLevel,
        final String pumpStatus,
        final String timestamp) {
      this.type = type;
      this.deviceId = deviceId;
      this.waterLevel = waterLevel;
      this.pumpStatus = pumpStatus;
      this.timestamp = timestamp;
    }

    public String getType() {
      return type;
    }

    public void setType(final String type) {
      this.type = type;
    }

    public Long getDeviceId() {
      return deviceId;
    }

    public void setDeviceId(final Long deviceId) {
      this.deviceId = deviceId;
    }

    public Double getWaterLevel() {
      return waterLevel;
    }

    public void setWaterLevel(final Double waterLevel) {
      this.waterLevel = waterLevel;
    }

    public String getPumpStatus() {
      return pumpStatus;
    }

    public void setPumpStatus(final String pumpStatus) {
      this.pumpStatus = pumpStatus;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(final String timestamp) {
      this.timestamp = timestamp;
    }
  }

  /** DTO for pump status message. */
  public static class PumpStatusMessage {
    private String type;
    private Long deviceId;
    private String pumpStatus;
    private String timestamp;

    public PumpStatusMessage(
        final String type, final Long deviceId, final String pumpStatus, final String timestamp) {
      this.type = type;
      this.deviceId = deviceId;
      this.pumpStatus = pumpStatus;
      this.timestamp = timestamp;
    }

    public String getType() {
      return type;
    }

    public void setType(final String type) {
      this.type = type;
    }

    public Long getDeviceId() {
      return deviceId;
    }

    public void setDeviceId(final Long deviceId) {
      this.deviceId = deviceId;
    }

    public String getPumpStatus() {
      return pumpStatus;
    }

    public void setPumpStatus(final String pumpStatus) {
      this.pumpStatus = pumpStatus;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(final String timestamp) {
      this.timestamp = timestamp;
    }
  }
}
