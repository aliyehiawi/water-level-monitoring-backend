package com.example.waterlevel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * Service for publishing MQTT messages to hardware devices.
 *
 * <p>Handles publishing pump commands and threshold updates to devices via MQTT.
 */
@Service
public class MqttService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttService.class);

  private final MessageChannel mqttOutboundChannel;
  private final ObjectMapper objectMapper;

  @Autowired
  public MqttService(final MessageChannel mqttOutboundChannel, final ObjectMapper objectMapper) {
    this.mqttOutboundChannel = mqttOutboundChannel;
    this.objectMapper = objectMapper;
  }

  /**
   * Publishes a pump start command to the specified device.
   *
   * @param deviceKey the device key (UUID) of the target device
   * @param initiatedBy the user ID who initiated the command
   * @return true if message was sent successfully, false otherwise
   */
  public boolean publishPumpStartCommand(final String deviceKey, final Long initiatedBy) {
    String topic = "devices/" + deviceKey + "/pump/start";
    PumpStartCommand command = new PumpStartCommand("START", Instant.now(), initiatedBy);

    return publishMessage(topic, command);
  }

  /**
   * Publishes threshold update to the specified device.
   *
   * @param deviceKey the device key (UUID) of the target device
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   * @param updatedBy the user ID who updated the thresholds
   * @return true if message was sent successfully, false otherwise
   */
  public boolean publishThresholdUpdate(
      final String deviceKey,
      final Double minThreshold,
      final Double maxThreshold,
      final Long updatedBy) {
    String topic = "devices/" + deviceKey + "/thresholds/update";
    ThresholdUpdateCommand command =
        new ThresholdUpdateCommand(minThreshold, maxThreshold, Instant.now(), updatedBy);

    return publishMessage(topic, command);
  }

  private boolean publishMessage(final String topic, final Object payload) {
    try {
      String jsonPayload = objectMapper.writeValueAsString(payload);

      mqttOutboundChannel.send(
          MessageBuilder.withPayload(jsonPayload.getBytes())
              .setHeader("mqtt_topic", topic)
              .setHeader("mqtt_qos", 1)
              .build());

      LOGGER.info("MQTT message published to topic: {} with payload: {}", topic, jsonPayload);
      return true;
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialize MQTT message payload", e);
      return false;
    } catch (Exception e) {
      LOGGER.error("Failed to publish MQTT message to topic: {}", topic, e);
      return false;
    }
  }

  /** DTO for pump start command. */
  public static class PumpStartCommand {
    private String command;
    private String timestamp;
    private Long initiatedBy;

    public PumpStartCommand(final String command, final Instant timestamp, final Long initiatedBy) {
      this.command = command;
      this.timestamp = timestamp.toString();
      this.initiatedBy = initiatedBy;
    }

    public String getCommand() {
      return command;
    }

    public void setCommand(final String command) {
      this.command = command;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(final String timestamp) {
      this.timestamp = timestamp;
    }

    public Long getInitiatedBy() {
      return initiatedBy;
    }

    public void setInitiatedBy(final Long initiatedBy) {
      this.initiatedBy = initiatedBy;
    }
  }

  /** DTO for threshold update command. */
  public static class ThresholdUpdateCommand {
    private Double minThreshold;
    private Double maxThreshold;
    private String timestamp;
    private Long updatedBy;

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

    public Double getMinThreshold() {
      return minThreshold;
    }

    public void setMinThreshold(final Double minThreshold) {
      this.minThreshold = minThreshold;
    }

    public Double getMaxThreshold() {
      return maxThreshold;
    }

    public void setMaxThreshold(final Double maxThreshold) {
      this.maxThreshold = maxThreshold;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(final String timestamp) {
      this.timestamp = timestamp;
    }

    public Long getUpdatedBy() {
      return updatedBy;
    }

    public void setUpdatedBy(final Long updatedBy) {
      this.updatedBy = updatedBy;
    }
  }
}
