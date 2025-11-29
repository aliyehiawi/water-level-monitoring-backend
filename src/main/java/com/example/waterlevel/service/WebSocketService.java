package com.example.waterlevel.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
  private static final String DESTINATION_PREFIX = "/topic/device/";

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
    SensorUpdateMessage message =
        new SensorUpdateMessage("sensor_update", deviceId, waterLevel, pumpStatus, timestamp);
    sendMessage(deviceId, message, "sensor update");
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
    PumpStatusMessage message =
        new PumpStatusMessage("pump_status", deviceId, pumpStatus, timestamp);
    sendMessage(deviceId, message, "pump status update");
  }

  /**
   * Sends threshold update confirmation to frontend.
   *
   * @param deviceId the device ID
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   */
  public void sendThresholdUpdateConfirmation(
      final Long deviceId, final Double minThreshold, final Double maxThreshold) {
    ThresholdUpdateMessage message =
        new ThresholdUpdateMessage("threshold_updated", deviceId, minThreshold, maxThreshold);
    sendMessage(deviceId, message, "threshold update");
  }

  /**
   * Common method to send WebSocket messages.
   *
   * @param deviceId the device ID
   * @param message the message object
   * @param messageType the type of message for logging
   */
  private void sendMessage(final Long deviceId, final Object message, final String messageType) {
    try {
      String destination = DESTINATION_PREFIX + deviceId;
      messagingTemplate.convertAndSend(destination, message);
      LOGGER.debug("WebSocket {} sent to {}: {}", messageType, destination, message);
    } catch (Exception e) {
      LOGGER.error("Failed to send WebSocket {} for device {}", messageType, deviceId, e);
    }
  }

  /** Base message class with common fields. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BaseMessage {
    private String type;
    private Long deviceId;
    private String timestamp;
  }

  /** DTO for sensor update message. */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class SensorUpdateMessage extends BaseMessage {
    private Double waterLevel;
    private String pumpStatus;

    public SensorUpdateMessage(
        final String type,
        final Long deviceId,
        final Double waterLevel,
        final String pumpStatus,
        final String timestamp) {
      super(type, deviceId, timestamp);
      this.waterLevel = waterLevel;
      this.pumpStatus = pumpStatus;
    }
  }

  /** DTO for pump status message. */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class PumpStatusMessage extends BaseMessage {
    private String pumpStatus;

    public PumpStatusMessage(
        final String type, final Long deviceId, final String pumpStatus, final String timestamp) {
      super(type, deviceId, timestamp);
      this.pumpStatus = pumpStatus;
    }
  }

  /** DTO for threshold update message. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThresholdUpdateMessage extends BaseMessage {
    private Double minThreshold;
    private Double maxThreshold;

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
}
