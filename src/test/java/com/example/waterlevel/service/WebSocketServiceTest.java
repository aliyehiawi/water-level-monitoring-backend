package com.example.waterlevel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.waterlevel.constants.WebSocketDestinations;
import com.example.waterlevel.dto.websocket.SensorUpdateMessage;
import com.example.waterlevel.dto.websocket.ThresholdUpdateMessage;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.service.impl.WebSocketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

  @Mock private SimpMessagingTemplate messagingTemplate;

  @InjectMocks private WebSocketServiceImpl webSocketService;

  @Test
  void sendSensorUpdate_SendsMessage() {
    Long deviceId = 1L;
    Double waterLevel = 50.5;
    PumpStatus pumpStatus = PumpStatus.ON;
    String timestamp = "2023-01-01T12:00:00";

    webSocketService.sendSensorUpdate(deviceId, waterLevel, pumpStatus, timestamp);

    ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
    verify(messagingTemplate)
        .convertAndSend(eq(WebSocketDestinations.deviceTopic(deviceId)), messageCaptor.capture());
    SensorUpdateMessage message = (SensorUpdateMessage) messageCaptor.getValue();
    assertEquals(deviceId, message.getDeviceId());
    assertEquals(waterLevel, message.getWaterLevel());
    assertEquals(PumpStatus.ON, message.getPumpStatus());
    assertEquals(timestamp, message.getTimestamp());
  }

  @Test
  void sendThresholdUpdateConfirmation_SendsMessage() {
    Long deviceId = 1L;
    Double minThreshold = 10.0;
    Double maxThreshold = 90.0;

    webSocketService.sendThresholdUpdateConfirmation(deviceId, minThreshold, maxThreshold);

    ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
    verify(messagingTemplate)
        .convertAndSend(eq(WebSocketDestinations.deviceTopic(deviceId)), messageCaptor.capture());
    ThresholdUpdateMessage message = (ThresholdUpdateMessage) messageCaptor.getValue();
    assertEquals(deviceId, message.getDeviceId());
    assertEquals(minThreshold, message.getMinThreshold());
    assertEquals(maxThreshold, message.getMaxThreshold());
  }

  @Test
  void sendSensorUpdate_Exception_LogsError() {
    Long deviceId = 1L;
    Double waterLevel = 50.5;
    PumpStatus pumpStatus = PumpStatus.ON;
    String timestamp = "2023-01-01T12:00:00";

    doThrow(new RuntimeException("Connection error"))
        .when(messagingTemplate)
        .convertAndSend(eq(WebSocketDestinations.deviceTopic(deviceId)), any(Object.class));

    webSocketService.sendSensorUpdate(deviceId, waterLevel, pumpStatus, timestamp);

    verify(messagingTemplate)
        .convertAndSend(eq(WebSocketDestinations.deviceTopic(deviceId)), any(Object.class));
  }
}
