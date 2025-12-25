package com.example.waterlevel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.service.WaterLevelDataService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaterLevelDataControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private WaterLevelDataService waterLevelDataService;

  @Test
  @WithMockUser(roles = "USER", username = "testuser")
  void getWaterLevelData_AsUser_SuccessAndPaginated() throws Exception {
    Long deviceId = 1L;
    Device device = new Device();
    device.setId(deviceId);

    WaterLevelData row = new WaterLevelData();
    row.setId(10L);
    row.setDevice(device);
    row.setWaterLevel(BigDecimal.valueOf(55.25));
    row.setPumpStatus(PumpStatus.OFF);
    row.setTimestamp(LocalDateTime.of(2025, 12, 25, 10, 0, 0));

    Page<WaterLevelData> page = new PageImpl<>(List.of(row), PageRequest.of(0, 1), 5);

    when(waterLevelDataService.getWaterLevelDataForDevice(eq(deviceId), any())).thenReturn(page);

    mockMvc
        .perform(get("/devices/1/water-level-data?page=0&size=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(10))
        .andExpect(jsonPath("$.content[0].deviceId").value(1))
        .andExpect(jsonPath("$.content[0].waterLevel").value(55.25))
        .andExpect(jsonPath("$.content[0].pumpStatus").value("OFF"))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.number").value(0));
  }
}
