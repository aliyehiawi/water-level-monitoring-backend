package com.example.waterlevel.service.impl;

import com.example.waterlevel.constants.ApplicationConstants;
import com.example.waterlevel.constants.MqttConstants;
import com.example.waterlevel.constants.MqttTopics;
import com.example.waterlevel.constants.ThreadPoolConstants;
import com.example.waterlevel.dto.mqtt.PumpStartCommand;
import com.example.waterlevel.dto.mqtt.ThresholdUpdateCommand;
import com.example.waterlevel.service.MqttService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * Service implementation for publishing MQTT messages to hardware devices.
 *
 * <p>Handles publishing pump commands and threshold updates to devices via MQTT with retry logic.
 */
@Service
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttServiceImpl implements MqttService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttServiceImpl.class);

  private final MessageChannel mqttOutboundChannel;
  private final ObjectMapper objectMapper;
  private ScheduledExecutorService scheduler;

  @Value("${mqtt.retry.max-attempts:" + ApplicationConstants.DEFAULT_MQTT_RETRY_MAX_ATTEMPTS + "}")
  private int maxRetryAttempts;

  @Value(
      "${mqtt.retry.initial-delay-ms:"
          + ApplicationConstants.DEFAULT_MQTT_RETRY_INITIAL_DELAY_MS
          + "}")
  private long initialDelayMs;

  @Value("${mqtt.retry.max-delay-ms:" + ApplicationConstants.DEFAULT_MQTT_RETRY_MAX_DELAY_MS + "}")
  private long maxDelayMs;

  @Value("${mqtt.retry.multiplier:" + ApplicationConstants.DEFAULT_MQTT_RETRY_MULTIPLIER + "}")
  private double multiplier;

  @Value("${mqtt.scheduler.pool-size:" + ThreadPoolConstants.DEFAULT_MQTT_SCHEDULER_POOL_SIZE + "}")
  private int schedulerPoolSize;

  public MqttServiceImpl(
      final MessageChannel mqttOutboundChannel, final ObjectMapper objectMapper) {
    this.mqttOutboundChannel = mqttOutboundChannel;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    this.scheduler = Executors.newScheduledThreadPool(schedulerPoolSize);
  }

  @PreDestroy
  public void shutdown() {
    if (scheduler != null) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(
            ApplicationConstants.MQTT_SCHEDULER_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
        LOGGER.warn("MQTT scheduler shutdown was interrupted");
      }
    }
  }

  /**
   * Publishes a pump start command to the specified device.
   *
   * @param deviceKey the device key (UUID) of the target device
   * @param initiatedBy the user ID who initiated the command
   * @return true if message was sent successfully, false otherwise
   */
  @Override
  public boolean publishPumpStartCommand(final String deviceKey, final Long initiatedBy) {
    String topic = MqttTopics.pumpStartTopic(deviceKey);
    PumpStartCommand command =
        new PumpStartCommand(MqttConstants.PUMP_START_COMMAND, Instant.now(), initiatedBy);

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
  @Override
  public boolean publishThresholdUpdate(
      final String deviceKey,
      final Double minThreshold,
      final Double maxThreshold,
      final Long updatedBy) {
    String topic = MqttTopics.thresholdUpdateTopic(deviceKey);
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
    String jsonPayload;
    try {
      jsonPayload = objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialize MQTT message payload for topic: {}", topic, e);
      return false;
    }

    try {
      mqttOutboundChannel.send(
          MessageBuilder.withPayload(jsonPayload.getBytes(StandardCharsets.UTF_8))
              .setHeader("mqtt_topic", topic)
              .setHeader("mqtt_qos", MqttConstants.DEFAULT_QOS_LEVEL)
              .build());
      LOGGER.debug("MQTT message published to topic: {} with payload: {}", topic, jsonPayload);
      return true;
    } catch (Exception e) {
      LOGGER.warn(
          "Failed to publish MQTT message to topic: {} (attempt 1/{}): {}",
          topic,
          maxRetryAttempts,
          e.getMessage());

      if (maxRetryAttempts <= 1) {
        LOGGER.error("Failed to publish MQTT message to topic: {} after 1 attempt", topic, e);
        return false;
      }
    }

    CompletableFuture<Boolean> future = new CompletableFuture<>();
    long delay = initialDelayMs;

    for (int attempt = 2; attempt <= maxRetryAttempts; attempt++) {
      final int currentAttempt = attempt;
      final long currentDelay = delay;

      scheduler.schedule(
          () -> {
            try {
              mqttOutboundChannel.send(
                  MessageBuilder.withPayload(jsonPayload.getBytes(StandardCharsets.UTF_8))
                      .setHeader("mqtt_topic", topic)
                      .setHeader("mqtt_qos", MqttConstants.DEFAULT_QOS_LEVEL)
                      .build());
              LOGGER.info(
                  "MQTT message published to topic: {} after {} attempt(s)", topic, currentAttempt);
              future.complete(true);
            } catch (Exception e) {
              LOGGER.warn(
                  "Failed to publish MQTT message to topic: {} (attempt {}/{}): {}",
                  topic,
                  currentAttempt,
                  maxRetryAttempts,
                  e.getMessage());
              if (currentAttempt >= maxRetryAttempts) {
                LOGGER.error(
                    "Failed to publish MQTT message to topic: {} after {} attempts",
                    topic,
                    maxRetryAttempts,
                    e);
                future.complete(false);
              }
            }
          },
          currentDelay,
          TimeUnit.MILLISECONDS);

      delay = Math.min((long) (delay * multiplier), maxDelayMs);
    }

    try {
      return future.get(maxDelayMs * maxRetryAttempts, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("MQTT retry interrupted for topic: {}", topic, e);
      return false;
    } catch (Exception e) {
      LOGGER.error("Error waiting for MQTT retry result for topic: {}", topic, e);
      return false;
    }
  }
}
