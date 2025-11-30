package com.example.waterlevel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.waterlevel.service.impl.MqttServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MqttServiceTest {

  @Mock private MessageChannel mqttOutboundChannel;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private MqttServiceImpl mqttService;

  @BeforeEach
  void setUp() throws Exception {
    ReflectionTestUtils.setField(mqttService, "maxRetryAttempts", 3);
    ReflectionTestUtils.setField(mqttService, "initialDelayMs", 10L);
    ReflectionTestUtils.setField(mqttService, "maxDelayMs", 100L);
    ReflectionTestUtils.setField(mqttService, "multiplier", 2.0);
    ReflectionTestUtils.setField(mqttService, "schedulerPoolSize", 2);
    ReflectionTestUtils.invokeMethod(mqttService, "init");
    when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
  }

  @Test
  void publishPumpStartCommand_Success() {
    // Arrange
    when(mqttOutboundChannel.send(any(Message.class))).thenReturn(true);

    // Act
    boolean result = mqttService.publishPumpStartCommand("device-key", 1L);

    // Assert
    assertTrue(result);
    verify(mqttOutboundChannel).send(any(Message.class));
  }

  @Test
  void publishPumpStartCommand_Failure_ReturnsFalse() {
    // Arrange
    when(mqttOutboundChannel.send(any(Message.class)))
        .thenThrow(new RuntimeException("MQTT error"));

    // Act
    boolean result = mqttService.publishPumpStartCommand("device-key", 1L);

    // Assert
    assertFalse(result);
  }

  @Test
  void publishThresholdUpdate_Success() {
    // Arrange
    when(mqttOutboundChannel.send(any(Message.class))).thenReturn(true);

    // Act
    boolean result = mqttService.publishThresholdUpdate("device-key", 10.0, 90.0, 1L);

    // Assert
    assertTrue(result);
    verify(mqttOutboundChannel).send(any(Message.class));
  }

  @Test
  void publishThresholdUpdate_SerializationError_ReturnsFalse() throws Exception {
    // Arrange
    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Error") {});

    // Act
    boolean result = mqttService.publishThresholdUpdate("device-key", 10.0, 90.0, 1L);

    // Assert
    assertFalse(result);
    verify(mqttOutboundChannel, never()).send(any(Message.class));
  }
}
