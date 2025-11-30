package com.example.waterlevel.service.impl;

import com.example.waterlevel.constants.ApplicationConstants;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.SensorDataService;
import com.example.waterlevel.service.WebSocketService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Service implementation for processing sensor data received from hardware via MQTT.
 *
 * <p>Receives MQTT messages, validates device keys, stores data, and broadcasts to frontend.
 */
@Service
public class SensorDataServiceImpl implements SensorDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataServiceImpl.class);

  private final DeviceRepository deviceRepository;
  private final WaterLevelDataRepository waterLevelDataRepository;
  private final WebSocketService webSocketService;
  private final ObjectMapper objectMapper;

  public SensorDataServiceImpl(
      final DeviceRepository deviceRepository,
      final WaterLevelDataRepository waterLevelDataRepository,
      final WebSocketService webSocketService,
      final ObjectMapper objectMapper) {
    this.deviceRepository = deviceRepository;
    this.waterLevelDataRepository = waterLevelDataRepository;
    this.webSocketService = webSocketService;
    this.objectMapper = objectMapper;
  }

  /**
   * Processes incoming MQTT sensor data messages.
   *
   * @param message the MQTT message containing sensor data
   */
  @Override
  @ServiceActivator(inputChannel = "mqttInboundChannel")
  public void processSensorData(final Message<byte[]> message) {
    String topic = null;
    try {
      topic = (String) message.getHeaders().get("mqtt_receivedTopic");
      String payload = new String(message.getPayload());

      LOGGER.debug("Received MQTT message from topic: {} with payload: {}", topic, payload);

      // Parse JSON payload
      JsonNode jsonNode = objectMapper.readTree(payload);

      // Validate required fields exist
      if (!jsonNode.has("device_key")) {
        throw new IllegalArgumentException("Missing device_key in MQTT message");
      }
      if (!jsonNode.has("water_level")) {
        throw new IllegalArgumentException("Missing water_level in MQTT message");
      }
      if (!jsonNode.has("pump_status")) {
        throw new IllegalArgumentException("Missing pump_status in MQTT message");
      }

      // Extract and validate device_key
      String deviceKeyRaw =
          jsonNode.get("device_key").isNull() ? null : jsonNode.get("device_key").asText();
      if (deviceKeyRaw == null || deviceKeyRaw.trim().isEmpty()) {
        throw new IllegalArgumentException("Missing or empty device_key in MQTT message");
      }
      final String deviceKey = deviceKeyRaw.trim();

      // Extract and validate water level
      if (jsonNode.get("water_level").isNull()) {
        throw new IllegalArgumentException("Missing or null water_level in MQTT message");
      }
      Double waterLevel = jsonNode.get("water_level").asDouble();

      // Extract and validate pump_status
      String pumpStatusRaw =
          jsonNode.get("pump_status").isNull() ? null : jsonNode.get("pump_status").asText();
      if (pumpStatusRaw == null || pumpStatusRaw.trim().isEmpty()) {
        throw new IllegalArgumentException("Missing or empty pump_status in MQTT message");
      }
      final String pumpStatus = pumpStatusRaw.trim();
      String timestampStr = jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : null;

      // Validate water level is not NaN or Infinity
      if (Double.isNaN(waterLevel) || Double.isInfinite(waterLevel)) {
        throw new IllegalArgumentException("Water level cannot be NaN or Infinity");
      }

      // Validate water level range
      if (waterLevel < ApplicationConstants.MIN_WATER_LEVEL
          || waterLevel > ApplicationConstants.MAX_WATER_LEVEL) {
        throw new IllegalArgumentException("Water level out of valid range");
      }

      // Validate and convert pump status
      PumpStatus pumpStatusEnum = PumpStatus.fromString(pumpStatus);
      if (pumpStatusEnum == PumpStatus.UNKNOWN && !pumpStatus.equalsIgnoreCase("UNKNOWN")) {
        throw new IllegalArgumentException("Invalid pump status. Must be ON, OFF, or UNKNOWN");
      }

      // Validate device key format (UUID format)
      if (deviceKey.length() != ApplicationConstants.UUID_LENGTH
          || !deviceKey.matches(
              "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
        throw new IllegalArgumentException("Invalid device key format");
      }

      // Validate device exists
      Device device =
          deviceRepository
              .findByDeviceKey(deviceKey)
              .orElseThrow(() -> new IllegalArgumentException("Device not found"));

      // Create and save WaterLevelData
      WaterLevelData data = new WaterLevelData();
      data.setDevice(device);
      data.setWaterLevel(BigDecimal.valueOf(waterLevel));
      data.setPumpStatus(pumpStatusEnum);
      if (timestampStr != null) {
        try {
          data.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception e) {
          LOGGER.warn(
              "Invalid timestamp format in MQTT message: {}, using current time", timestampStr);
          data.setTimestamp(LocalDateTime.now());
        }
      } else {
        data.setTimestamp(LocalDateTime.now());
      }
      waterLevelDataRepository.save(data);

      LOGGER.info(
          "Sensor data stored for device {}: water_level={}, pump_status={}",
          device.getId(),
          waterLevel,
          pumpStatusEnum);

      // Broadcast to frontend via WebSocket
      String timestamp = data.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME);
      webSocketService.sendSensorUpdate(device.getId(), waterLevel, pumpStatusEnum, timestamp);

    } catch (IllegalArgumentException e) {
      LOGGER.warn("Invalid sensor data from topic {}: {}", topic, e.getMessage());
      // Don't re-throw validation errors - they're expected for invalid MQTT messages
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      LOGGER.error("Failed to parse MQTT message JSON from topic {}: {}", topic, e.getMessage(), e);
      // Don't re-throw JSON parsing errors - log and continue
    } catch (Exception e) {
      LOGGER.error("Unexpected error processing sensor data from topic: {}", topic, e);
      // Re-throw unexpected errors to allow upper layers to handle critical failures
      throw e;
    }
  }
}
