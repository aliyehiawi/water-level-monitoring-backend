package com.example.waterlevel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeviceControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private DeviceService deviceService;
  @MockBean private UserRepository userRepository;
  @MockBean private AuditService auditService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void registerDevice_Success() throws Exception {
    DeviceRegisterRequest request = new DeviceRegisterRequest();
    request.setName("Test Device");
    request.setMinThreshold(10.0);
    request.setMaxThreshold(90.0);

    Device device = new Device();
    device.setId(1L);
    device.setName("Test Device");
    device.setDeviceKey("test-key");
    device.setMinThreshold(BigDecimal.valueOf(10.0));
    device.setMaxThreshold(BigDecimal.valueOf(90.0));
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    device.setAdmin(admin);
    device.setCreatedAt(LocalDateTime.now());
    device.setUpdatedAt(LocalDateTime.now());

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.registerDevice(any(), any())).thenReturn(device);

    mockMvc
        .perform(
            post("/devices/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Test Device"));
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void getAllDevices_Success() throws Exception {
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setName("Test Device");
    device.setDeviceKey("test-key");
    device.setMinThreshold(BigDecimal.valueOf(10.0));
    device.setMaxThreshold(BigDecimal.valueOf(90.0));
    device.setAdmin(admin);
    device.setCreatedAt(LocalDateTime.now());
    List<Device> devices = Arrays.asList(device);
    Page<Device> devicePage = new PageImpl<>(devices, PageRequest.of(0, 20), 1);
    when(deviceService.getAllDevices(any(Pageable.class))).thenReturn(devicePage);

    mockMvc
        .perform(get("/devices"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Test Device"));
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void deleteDevice_Success() throws Exception {
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    Device device = new Device();
    device.setId(1L);
    device.setAdmin(admin);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(deviceService.validateDeviceOwnership(1L, 1L)).thenReturn(device);

    mockMvc.perform(delete("/devices/1")).andExpect(status().isNoContent());

    verify(deviceService).deleteDevice(1L);
  }
}
