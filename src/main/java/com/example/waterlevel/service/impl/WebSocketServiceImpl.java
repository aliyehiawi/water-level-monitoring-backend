package com.example.waterlevel.service.impl;

import com.example.waterlevel.constants.MessageTypes;
import com.example.waterlevel.constants.WebSocketDestinations;
import com.example.waterlevel.dto.websocket.PumpStatusMessage;
import com.example.waterlevel.dto.websocket.SensorUpdateMessage;
import com.example.waterlevel.dto.websocket.ThresholdUpdateMessage;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for sending real-time updates to frontend via WebSocket.
 *
 * <p>Broadcasts sensor data updates and device status changes to connected WebSocket clients.
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServiceImpl.class);

  private final SimpMessagingTemplate messagingTemplate;

  public WebSocketServiceImpl(final SimpMessagingTemplate messagingTemplate) {
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
  @Override
  public void sendSensorUpdate(
      final Long deviceId,
      final Double waterLevel,
      final PumpStatus pumpStatus,
      final String timestamp) {
    SensorUpdateMessage message =
        new SensorUpdateMessage(
            MessageTypes.SENSOR_UPDATE, deviceId, waterLevel, pumpStatus, timestamp);
    sendMessage(deviceId, message, "sensor update");
  }

  /**
   * Sends pump status change notification to frontend.
   *
   * @param deviceId the device ID
   * @param pumpStatus the new pump status
   * @param timestamp the timestamp
   */
  @Override
  public void sendPumpStatusUpdate(
      final Long deviceId, final PumpStatus pumpStatus, final String timestamp) {
    PumpStatusMessage message =
        new PumpStatusMessage(MessageTypes.PUMP_STATUS, deviceId, pumpStatus, timestamp);
    sendMessage(deviceId, message, "pump status update");
  }

  /**
   * Sends threshold update confirmation to frontend.
   *
   * @param deviceId the device ID
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   */
  @Override
  public void sendThresholdUpdateConfirmation(
      final Long deviceId, final Double minThreshold, final Double maxThreshold) {
    ThresholdUpdateMessage message =
        new ThresholdUpdateMessage(
            MessageTypes.THRESHOLD_UPDATED, deviceId, minThreshold, maxThreshold);
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
      String destination = WebSocketDestinations.deviceTopic(deviceId);
      messagingTemplate.convertAndSend(destination, message);
      LOGGER.debug("WebSocket {} sent to {}: {}", messageType, destination, message);
    } catch (Exception e) {
      LOGGER.error("Failed to send WebSocket {} for device {}", messageType, deviceId, e);
    }
  }
}
