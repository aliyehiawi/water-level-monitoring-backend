package com.example.waterlevel.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.dto.ThresholdUpdateRequest;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeviceManagementFlowIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private DeviceRepository deviceRepository;
  @Autowired private WaterLevelDataRepository waterLevelDataRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private String adminToken;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.deleteAll();
    deviceRepository.deleteAll();
    waterLevelDataRepository.deleteAll();

    // Create admin user
    User admin = new User();
    admin.setUsername("admin");
    admin.setEmail("admin@example.com");
    admin.setPassword(passwordEncoder.encode("password123"));
    admin.setRole(User.Role.ADMIN);
    userRepository.save(admin);

    // Login as admin
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setUsername("admin");
    loginRequest.setPassword("password123");

    String loginResponse =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
    adminToken = authResponse.getToken();
    assertNotNull(adminToken);
  }

  @Test
  void completeDeviceManagementFlow() throws Exception {
    // Step 1: Register a device
    DeviceRegisterRequest deviceRequest = new DeviceRegisterRequest();
    deviceRequest.setName("Test Device");
    deviceRequest.setMinThreshold(10.0);
    deviceRequest.setMaxThreshold(90.0);

    String deviceResponse =
        mockMvc
            .perform(
                post("/api/devices/register")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deviceRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Device"))
            .andExpect(jsonPath("$.deviceKey").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Extract device ID from response
    com.fasterxml.jackson.databind.JsonNode deviceJson = objectMapper.readTree(deviceResponse);
    Long deviceId = deviceJson.get("id").asLong();

    // Step 2: Get all devices
    mockMvc
        .perform(get("/api/devices").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(deviceId));

    // Step 3: Get thresholds
    mockMvc
        .perform(
            get("/api/devices/" + deviceId + "/thresholds")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.minThreshold").value(10.0))
        .andExpect(jsonPath("$.maxThreshold").value(90.0));

    // Step 4: Update thresholds
    ThresholdUpdateRequest thresholdRequest = new ThresholdUpdateRequest();
    thresholdRequest.setMinThreshold(15.0);
    thresholdRequest.setMaxThreshold(85.0);

    mockMvc
        .perform(
            put("/api/devices/" + deviceId + "/thresholds")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(thresholdRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.minThreshold").value(15.0))
        .andExpect(jsonPath("$.maxThreshold").value(85.0));

    // Step 5: Start pump
    mockMvc
        .perform(
            post("/api/devices/" + deviceId + "/pump/start")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());

    // Step 6: Get pump status
    mockMvc
        .perform(
            get("/api/devices/" + deviceId + "/pump/status")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pumpStatus").exists());
  }
}
