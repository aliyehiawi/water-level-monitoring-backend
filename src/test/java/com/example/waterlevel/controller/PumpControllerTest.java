package com.example.waterlevel.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.MqttService;
import com.example.waterlevel.service.PumpService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PumpControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private DeviceService deviceService;
  @MockBean private MqttService mqttService;
  @MockBean private PumpService pumpService;
  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void startPump_Success() throws Exception {
    // Arrange
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setDeviceKey("test-key");
    device.setAdmin(admin);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);
    when(mqttService.publishPumpStartCommand(anyString(), anyLong())).thenReturn(true);

    // Act & Assert
    mockMvc.perform(post("/api/devices/1/pump/start")).andExpect(status().isOk());

    verify(mqttService).publishPumpStartCommand("test-key", 1L);
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void startPump_MqttFailure_ReturnsInternalServerError() throws Exception {
    // Arrange
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setDeviceKey("test-key");
    device.setAdmin(admin);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);
    when(mqttService.publishPumpStartCommand(anyString(), anyLong())).thenReturn(false);

    // Act & Assert
    mockMvc.perform(post("/api/devices/1/pump/start")).andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void getPumpStatus_Success() throws Exception {
    // Arrange
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setAdmin(admin);
    WaterLevelData data = new WaterLevelData();
    data.setPumpStatus(PumpStatus.ON);
    data.setTimestamp(LocalDateTime.now());

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);
    when(pumpService.getCurrentPumpStatus(device)).thenReturn(PumpStatus.ON);
    when(pumpService.getLatestData(device)).thenReturn(Optional.of(data));

    // Act & Assert
    mockMvc
        .perform(get("/api/devices/1/pump/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pumpStatus").value("ON"));
  }
}
