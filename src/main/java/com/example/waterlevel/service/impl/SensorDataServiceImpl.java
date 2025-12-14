package com.example.waterlevel.service.impl;

import com.example.waterlevel.constants.ApplicationConstants;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.exception.SensorDataProcessingException;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.SensorDataService;
import com.example.waterlevel.service.WebSocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

  private static final String DEVICE_KEY_FIELD = "device_key";
  private static final String WATER_LEVEL_FIELD = "water_level";
  private static final String PUMP_STATUS_FIELD = "pump_status";

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

      JsonNode jsonNode = objectMapper.readTree(payload);
      validateRequiredFields(jsonNode);

      final String deviceKey = extractAndValidateDeviceKey(jsonNode);
      Double waterLevel = extractAndValidateWaterLevel(jsonNode);
      final String pumpStatus = extractAndValidatePumpStatus(jsonNode);
      String timestampStr = extractTimestamp(jsonNode);

      validateWaterLevel(waterLevel);
      PumpStatus pumpStatusEnum = validateAndConvertPumpStatus(pumpStatus);
      Device device = validateAndGetDevice(deviceKey);

      WaterLevelData data =
          createAndSaveWaterLevelData(device, waterLevel, pumpStatusEnum, timestampStr);

      LOGGER.info(
          "Sensor data stored for device {}: water_level={}, pump_status={}",
          device.getId(),
          waterLevel,
          pumpStatusEnum);

      broadcastSensorUpdate(data, device.getId(), waterLevel, pumpStatusEnum);

    } catch (IllegalArgumentException e) {
      LOGGER.warn("Invalid sensor data from topic {}: {}", topic, e.getMessage());
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to parse MQTT message JSON from topic {}: {}", topic, e.getMessage(), e);
    } catch (SensorDataProcessingException e) {
      LOGGER.error("Sensor data processing error from topic {}: {}", topic, e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      LOGGER.error("Unexpected error processing sensor data from topic: {}", topic, e);
      throw new SensorDataProcessingException(
          "Failed to process sensor data from topic: " + topic, e);
    }
  }

  private void validateRequiredFields(final JsonNode jsonNode) {
    if (!jsonNode.has(DEVICE_KEY_FIELD)) {
      throw new IllegalArgumentException("Missing " + DEVICE_KEY_FIELD + " in MQTT message");
    }
    if (!jsonNode.has(WATER_LEVEL_FIELD)) {
      throw new IllegalArgumentException("Missing " + WATER_LEVEL_FIELD + " in MQTT message");
    }
    if (!jsonNode.has(PUMP_STATUS_FIELD)) {
      throw new IllegalArgumentException("Missing " + PUMP_STATUS_FIELD + " in MQTT message");
    }
  }

  private String extractAndValidateDeviceKey(final JsonNode jsonNode) {
    String deviceKeyRaw =
        jsonNode.get(DEVICE_KEY_FIELD).isNull() ? null : jsonNode.get(DEVICE_KEY_FIELD).asText();
    if (deviceKeyRaw == null || deviceKeyRaw.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Missing or empty " + DEVICE_KEY_FIELD + " in MQTT message");
    }
    return deviceKeyRaw.trim();
  }

  private Double extractAndValidateWaterLevel(final JsonNode jsonNode) {
    if (jsonNode.get(WATER_LEVEL_FIELD).isNull()) {
      throw new IllegalArgumentException(
          "Missing or null " + WATER_LEVEL_FIELD + " in MQTT message");
    }
    return jsonNode.get(WATER_LEVEL_FIELD).asDouble();
  }

  private String extractAndValidatePumpStatus(final JsonNode jsonNode) {
    String pumpStatusRaw =
        jsonNode.get(PUMP_STATUS_FIELD).isNull() ? null : jsonNode.get(PUMP_STATUS_FIELD).asText();
    if (pumpStatusRaw == null || pumpStatusRaw.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Missing or empty " + PUMP_STATUS_FIELD + " in MQTT message");
    }
    return pumpStatusRaw.trim();
  }

  private String extractTimestamp(final JsonNode jsonNode) {
    return jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : null;
  }

  private void validateWaterLevel(final Double waterLevel) {
    if (Double.isNaN(waterLevel) || Double.isInfinite(waterLevel)) {
      throw new IllegalArgumentException("Water level cannot be NaN or Infinity");
    }
    if (waterLevel < ApplicationConstants.MIN_WATER_LEVEL
        || waterLevel > ApplicationConstants.MAX_WATER_LEVEL) {
      throw new IllegalArgumentException("Water level out of valid range");
    }
  }

  private PumpStatus validateAndConvertPumpStatus(final String pumpStatus) {
    PumpStatus pumpStatusEnum = PumpStatus.fromString(pumpStatus);
    if (pumpStatusEnum == PumpStatus.UNKNOWN && !pumpStatus.equalsIgnoreCase("UNKNOWN")) {
      throw new IllegalArgumentException("Invalid pump status. Must be ON, OFF, or UNKNOWN");
    }
    return pumpStatusEnum;
  }

  private Device validateAndGetDevice(final String deviceKey) {
    if (deviceKey.length() != ApplicationConstants.UUID_LENGTH
        || !deviceKey.matches(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
      throw new IllegalArgumentException("Invalid device key format");
    }
    return deviceRepository
        .findByDeviceKey(deviceKey)
        .orElseThrow(
            () -> new IllegalArgumentException(ApplicationConstants.DEVICE_NOT_FOUND_MESSAGE));
  }

  private WaterLevelData createAndSaveWaterLevelData(
      final Device device,
      final Double waterLevel,
      final PumpStatus pumpStatusEnum,
      final String timestampStr) {
    WaterLevelData data = new WaterLevelData();
    data.setDevice(device);
    data.setWaterLevel(BigDecimal.valueOf(waterLevel));
    data.setPumpStatus(pumpStatusEnum);
    data.setTimestamp(parseTimestamp(timestampStr));
    return waterLevelDataRepository.save(data);
  }

  private LocalDateTime parseTimestamp(final String timestampStr) {
    if (timestampStr == null) {
      return LocalDateTime.now();
    }
    try {
      return LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
    } catch (Exception e) {
      LOGGER.warn("Invalid timestamp format in MQTT message: {}, using current time", timestampStr);
      return LocalDateTime.now();
    }
  }

  private void broadcastSensorUpdate(
      final WaterLevelData data,
      final Long deviceId,
      final Double waterLevel,
      final PumpStatus pumpStatusEnum) {
    String timestamp = data.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME);
    webSocketService.sendSensorUpdate(deviceId, waterLevel, pumpStatusEnum, timestamp);
  }
}
