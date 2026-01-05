package com.example.waterlevel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.impl.SensorDataServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
class SensorDataServiceTest {

  @Mock private DeviceRepository deviceRepository;
  @Mock private WaterLevelDataRepository waterLevelDataRepository;
  @Mock private WebSocketService webSocketService;
  @Mock private ObjectMapper objectMapper;
  @Mock private Message<byte[]> message;
  @Mock private MessageHeaders messageHeaders;
  @Mock private JsonNode jsonNode;
  @Mock private JsonNode deviceKeyNode;
  @Mock private JsonNode waterLevelNode;
  @Mock private JsonNode pumpStatusNode;

  @InjectMocks private SensorDataServiceImpl sensorDataService;

  private Device device;
  private User admin;

  @BeforeEach
  void setUp() {
    admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");

    device = new Device();
    device.setId(1L);
    device.setName("Test Device");
    device.setDeviceKey("123e4567-e89b-12d3-a456-426614174000");
    device.setAdmin(admin);
  }

  @Test
  void processSensorData_ValidData_SavesAndBroadcasts() throws Exception {
    String payload =
        "{\"device_key\":\"123e4567-e89b-12d3-a456-426614174000\",\"water_level\":50.5,\"pump_status\":\"ON\"}";
    byte[] payloadBytes = payload.getBytes();

    when(message.getHeaders()).thenReturn(messageHeaders);
    when(messageHeaders.get("mqtt_receivedTopic"))
        .thenReturn("devices/123e4567-e89b-12d3-a456-426614174000/sensor/data");
    when(message.getPayload()).thenReturn(payloadBytes);
    when(objectMapper.readTree(payload)).thenReturn(jsonNode);

    when(jsonNode.has("device_key")).thenReturn(true);
    when(jsonNode.get("device_key")).thenReturn(deviceKeyNode);
    when(deviceKeyNode.isNull()).thenReturn(false);
    when(deviceKeyNode.asText()).thenReturn("123e4567-e89b-12d3-a456-426614174000");
    when(jsonNode.has("water_level")).thenReturn(true);
    when(jsonNode.get("water_level")).thenReturn(waterLevelNode);
    when(waterLevelNode.isNull()).thenReturn(false);
    when(waterLevelNode.asDouble()).thenReturn(50.5);
    when(jsonNode.has("pump_status")).thenReturn(true);
    when(jsonNode.get("pump_status")).thenReturn(pumpStatusNode);
    when(pumpStatusNode.isNull()).thenReturn(false);
    when(pumpStatusNode.asText()).thenReturn("ON");
    when(jsonNode.has("timestamp")).thenReturn(false);

    when(deviceRepository.findByDeviceKey("123e4567-e89b-12d3-a456-426614174000"))
        .thenReturn(Optional.of(device));
    when(waterLevelDataRepository.save(any(WaterLevelData.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    sensorDataService.processSensorData(message);

    verify(deviceRepository).findByDeviceKey("123e4567-e89b-12d3-a456-426614174000");
    ArgumentCaptor<WaterLevelData> dataCaptor = ArgumentCaptor.forClass(WaterLevelData.class);
    verify(waterLevelDataRepository).save(dataCaptor.capture());
    WaterLevelData savedData = dataCaptor.getValue();
    assertEquals(device, savedData.getDevice());
    assertEquals(BigDecimal.valueOf(50.5), savedData.getWaterLevel());
    assertEquals(PumpStatus.ON, savedData.getPumpStatus());
    verify(webSocketService).sendSensorUpdate(eq(1L), eq(50.5), eq(PumpStatus.ON), anyString());
  }

  @Test
  void processSensorData_InvalidDeviceKey_LogsWarning() throws Exception {
    String payload = "{\"device_key\":\"invalid-key\",\"water_level\":50.5,\"pump_status\":\"ON\"}";
    byte[] payloadBytes = payload.getBytes();

    when(message.getHeaders()).thenReturn(messageHeaders);
    when(messageHeaders.get("mqtt_receivedTopic")).thenReturn("devices/invalid/sensor/data");
    when(message.getPayload()).thenReturn(payloadBytes);
    when(objectMapper.readTree(payload)).thenReturn(jsonNode);

    when(jsonNode.has("device_key")).thenReturn(true);
    when(jsonNode.has("water_level")).thenReturn(true);
    when(jsonNode.has("pump_status")).thenReturn(true);
    when(jsonNode.get("device_key")).thenReturn(deviceKeyNode);
    when(deviceKeyNode.isNull()).thenReturn(false);
    when(deviceKeyNode.asText()).thenReturn("invalid-key");
    // Device key format validation fails, but service still checks water_level existence
    lenient().when(jsonNode.get("water_level")).thenReturn(waterLevelNode);
    lenient().when(jsonNode.get("pump_status")).thenReturn(pumpStatusNode);

    sensorDataService.processSensorData(message);

    verify(deviceRepository, never()).findByDeviceKey(anyString());
    verify(waterLevelDataRepository, never()).save(any());
    verify(webSocketService, never()).sendSensorUpdate(anyLong(), anyDouble(), any(), anyString());
  }

  @Test
  void processSensorData_DeviceNotFound_LogsWarning() throws Exception {
    String payload =
        "{\"device_key\":\"123e4567-e89b-12d3-a456-426614174000\",\"water_level\":50.5,\"pump_status\":\"ON\"}";
    byte[] payloadBytes = payload.getBytes();

    when(message.getHeaders()).thenReturn(messageHeaders);
    when(messageHeaders.get("mqtt_receivedTopic"))
        .thenReturn("devices/123e4567-e89b-12d3-a456-426614174000/sensor/data");
    when(message.getPayload()).thenReturn(payloadBytes);
    when(objectMapper.readTree(payload)).thenReturn(jsonNode);

    when(jsonNode.has("device_key")).thenReturn(true);
    when(jsonNode.get("device_key")).thenReturn(deviceKeyNode);
    when(deviceKeyNode.isNull()).thenReturn(false);
    when(deviceKeyNode.asText()).thenReturn("123e4567-e89b-12d3-a456-426614174000");
    when(jsonNode.has("water_level")).thenReturn(true);
    when(jsonNode.get("water_level")).thenReturn(waterLevelNode);
    when(waterLevelNode.isNull()).thenReturn(false);
    when(waterLevelNode.asDouble()).thenReturn(50.5);
    when(jsonNode.has("pump_status")).thenReturn(true);
    when(jsonNode.get("pump_status")).thenReturn(pumpStatusNode);
    when(pumpStatusNode.isNull()).thenReturn(false);
    when(pumpStatusNode.asText()).thenReturn("ON");
    when(jsonNode.has("timestamp")).thenReturn(false);

    when(deviceRepository.findByDeviceKey("123e4567-e89b-12d3-a456-426614174000"))
        .thenReturn(Optional.empty());

    sensorDataService.processSensorData(message);

    verify(deviceRepository).findByDeviceKey("123e4567-e89b-12d3-a456-426614174000");
    verify(waterLevelDataRepository, never()).save(any());
    verify(webSocketService, never()).sendSensorUpdate(anyLong(), anyDouble(), any(), anyString());
  }

  @Test
  void processSensorData_InvalidPumpStatus_LogsWarning() throws Exception {
    String payload =
        "{\"device_key\":\"123e4567-e89b-12d3-a456-426614174000\",\"water_level\":50.5,\"pump_status\":\"INVALID\"}";
    byte[] payloadBytes = payload.getBytes();

    when(message.getHeaders()).thenReturn(messageHeaders);
    when(messageHeaders.get("mqtt_receivedTopic"))
        .thenReturn("devices/123e4567-e89b-12d3-a456-426614174000/sensor/data");
    when(message.getPayload()).thenReturn(payloadBytes);
    when(objectMapper.readTree(payload)).thenReturn(jsonNode);

    when(jsonNode.has("device_key")).thenReturn(true);
    when(jsonNode.get("device_key")).thenReturn(deviceKeyNode);
    when(deviceKeyNode.isNull()).thenReturn(false);
    when(deviceKeyNode.asText()).thenReturn("123e4567-e89b-12d3-a456-426614174000");
    when(jsonNode.has("water_level")).thenReturn(true);
    when(jsonNode.get("water_level")).thenReturn(waterLevelNode);
    when(waterLevelNode.isNull()).thenReturn(false);
    when(waterLevelNode.asDouble()).thenReturn(50.5);
    when(jsonNode.has("pump_status")).thenReturn(true);
    when(jsonNode.get("pump_status")).thenReturn(pumpStatusNode);
    when(pumpStatusNode.isNull()).thenReturn(false);
    when(pumpStatusNode.asText()).thenReturn("INVALID");

    sensorDataService.processSensorData(message);

    verify(deviceRepository, never()).findByDeviceKey(anyString());
    verify(waterLevelDataRepository, never()).save(any());
    verify(webSocketService, never()).sendSensorUpdate(anyLong(), anyDouble(), any(), anyString());
  }
}
