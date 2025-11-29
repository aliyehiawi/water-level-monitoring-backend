package com.example.waterlevel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * Service for publishing MQTT messages to hardware devices.
 *
 * <p>Handles publishing pump commands and threshold updates to devices via MQTT with retry logic.
 */
@Service
public class MqttService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttService.class);

  private final MessageChannel mqttOutboundChannel;
  private final ObjectMapper objectMapper;

  @Value("${spring.mqtt.retry.max-attempts:3}")
  private int maxRetryAttempts;

  @Value("${spring.mqtt.retry.initial-delay-ms:1000}")
  private long initialDelayMs;

  @Value("${spring.mqtt.retry.max-delay-ms:10000}")
  private long maxDelayMs;

  @Value("${spring.mqtt.retry.multiplier:2.0}")
  private double multiplier;

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

  /**
   * Publishes a message to MQTT with retry logic and exponential backoff.
   *
   * @param topic the MQTT topic
   * @param payload the message payload
   * @return true if message was sent successfully, false otherwise
   */
  private boolean publishMessage(final String topic, final Object payload) {
    // Serialize payload once (fail fast if serialization fails)
    String jsonPayload;
    try {
      jsonPayload = objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialize MQTT message payload for topic: {}", topic, e);
      return false;
    }

    // Retry logic with exponential backoff
    long delay = initialDelayMs;
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
      try {
        mqttOutboundChannel.send(
            MessageBuilder.withPayload(jsonPayload.getBytes())
                .setHeader("mqtt_topic", topic)
                .setHeader("mqtt_qos", 1)
                .build());

        if (attempt > 1) {
          LOGGER.info("MQTT message published to topic: {} after {} attempt(s)", topic, attempt);
        } else {
          LOGGER.debug("MQTT message published to topic: {} with payload: {}", topic, jsonPayload);
        }
        return true;

      } catch (Exception e) {
        lastException = e;
        LOGGER.warn(
            "Failed to publish MQTT message to topic: {} (attempt {}/{}): {}",
            topic,
            attempt,
            maxRetryAttempts,
            e.getMessage());

        // Don't wait after the last attempt
        if (attempt < maxRetryAttempts) {
          try {
            Thread.sleep(delay);
            delay = Math.min((long) (delay * multiplier), maxDelayMs);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.error("Retry delay interrupted for topic: {}", topic, ie);
            return false;
          }
        }
      }
    }

    // All retry attempts failed
    LOGGER.error(
        "Failed to publish MQTT message to topic: {} after {} attempts",
        topic,
        maxRetryAttempts,
        lastException);
    return false;
  }

  /** DTO for pump start command. */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class PumpStartCommand {
    private String command;
    private String timestamp;
    private Long initiatedBy;

    public PumpStartCommand(final String command, final Instant timestamp, final Long initiatedBy) {
      this.command = command;
      this.timestamp = timestamp.toString();
      this.initiatedBy = initiatedBy;
    }
  }

  /** DTO for threshold update command. */
  @Getter
  @Setter
  @NoArgsConstructor
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
  }
}
