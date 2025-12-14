package com.example.waterlevel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.waterlevel.dto.ThresholdUpdateRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.MqttService;
import com.example.waterlevel.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ThresholdControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private DeviceService deviceService;
  @MockBean private MqttService mqttService;
  @MockBean private UserRepository userRepository;
  @MockBean private WebSocketService webSocketService;
  @MockBean private AuditService auditService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void getThresholds_Success() throws Exception {
    // Arrange
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setMinThreshold(BigDecimal.valueOf(10.0));
    device.setMaxThreshold(BigDecimal.valueOf(90.0));
    device.setAdmin(admin);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);

    // Act & Assert
    mockMvc
        .perform(get("/devices/1/thresholds"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.minThreshold").value(10.0))
        .andExpect(jsonPath("$.maxThreshold").value(90.0));
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void updateThresholds_Success() throws Exception {
    // Arrange
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setDeviceKey("test-key");
    device.setMinThreshold(BigDecimal.valueOf(10.0));
    device.setMaxThreshold(BigDecimal.valueOf(90.0));
    device.setAdmin(admin);

    ThresholdUpdateRequest request = new ThresholdUpdateRequest();
    request.setMinThreshold(15.0);
    request.setMaxThreshold(85.0);

    Device updatedDevice = new Device();
    updatedDevice.setId(1L);
    updatedDevice.setDeviceKey("test-key");
    updatedDevice.setMinThreshold(BigDecimal.valueOf(15.0));
    updatedDevice.setMaxThreshold(BigDecimal.valueOf(85.0));
    updatedDevice.setAdmin(admin);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);
    when(deviceService.updateThresholds(
            any(Device.class), any(BigDecimal.class), any(BigDecimal.class)))
        .thenReturn(updatedDevice);
    when(mqttService.publishThresholdUpdate(anyString(), anyDouble(), anyDouble(), anyLong()))
        .thenReturn(true);
    doNothing()
        .when(webSocketService)
        .sendThresholdUpdateConfirmation(anyLong(), anyDouble(), anyDouble());
    doNothing()
        .when(auditService)
        .logThresholdUpdate(anyLong(), anyLong(), anyDouble(), anyDouble());

    // Act & Assert
    mockMvc
        .perform(
            put("/devices/1/thresholds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.minThreshold").value(15.0))
        .andExpect(jsonPath("$.maxThreshold").value(85.0));

    verify(mqttService).publishThresholdUpdate("test-key", 15.0, 85.0, 1L);
    verify(webSocketService).sendThresholdUpdateConfirmation(1L, 15.0, 85.0);
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void updateThresholds_InvalidThresholds_ReturnsBadRequest() throws Exception {
    // Arrange
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setAdmin(admin);

    ThresholdUpdateRequest request = new ThresholdUpdateRequest();
    request.setMinThreshold(90.0);
    request.setMaxThreshold(10.0); // Invalid: min >= max

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);

    // Act & Assert
    mockMvc
        .perform(
            put("/devices/1/thresholds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
