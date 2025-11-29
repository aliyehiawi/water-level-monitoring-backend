package com.example.waterlevel.service;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Service for processing sensor data received from hardware via MQTT.
 *
 * <p>Receives MQTT messages, validates device keys, stores data, and broadcasts to frontend.
 */
@Service
public class SensorDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataService.class);

  private final DeviceRepository deviceRepository;
  private final WaterLevelDataRepository waterLevelDataRepository;
  private final WebSocketService webSocketService;
  private final ObjectMapper objectMapper;

  @Autowired
  public SensorDataService(
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
  @ServiceActivator(inputChannel = "mqttInboundChannel")
  public void processSensorData(final Message<byte[]> message) {
    try {
      String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
      String payload = new String(message.getPayload());

      LOGGER.debug("Received MQTT message from topic: {} with payload: {}", topic, payload);

      // Parse JSON payload
      JsonNode jsonNode = objectMapper.readTree(payload);
      String deviceKey = jsonNode.get("device_key").asText();
      Double waterLevel = jsonNode.get("water_level").asDouble();
      String pumpStatus = jsonNode.get("pump_status").asText();
      String timestampStr = jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : null;

      // Validate device exists
      Device device =
          deviceRepository
              .findByDeviceKey(deviceKey)
              .orElseThrow(() -> new IllegalArgumentException("Invalid device key: " + deviceKey));

      // Create and save WaterLevelData
      WaterLevelData data = new WaterLevelData();
      data.setDevice(device);
      data.setWaterLevel(BigDecimal.valueOf(waterLevel));
      data.setPumpStatus(pumpStatus);
      if (timestampStr != null) {
        data.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME));
      }
      waterLevelDataRepository.save(data);

      LOGGER.info(
          "Sensor data stored for device {}: water_level={}, pump_status={}",
          device.getId(),
          waterLevel,
          pumpStatus);

      // Broadcast to frontend via WebSocket
      String timestamp = timestampStr != null ? timestampStr : LocalDateTime.now().toString();
      webSocketService.sendSensorUpdate(device.getId(), waterLevel, pumpStatus, timestamp);

    } catch (Exception e) {
      LOGGER.error("Failed to process sensor data from MQTT", e);
    }
  }
}
