package com.example.waterlevel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.impl.PumpServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PumpServiceTest {

  @Mock private WaterLevelDataRepository waterLevelDataRepository;

  @InjectMocks private PumpServiceImpl pumpService;

  private Device testDevice;
  private WaterLevelData testData;

  @BeforeEach
  void setUp() {
    testDevice = new Device();
    testDevice.setId(1L);
    testDevice.setName("Test Device");

    testData = new WaterLevelData();
    testData.setId(1L);
    testData.setDevice(testDevice);
    testData.setWaterLevel(BigDecimal.valueOf(50.5));
    testData.setPumpStatus(PumpStatus.ON);
    testData.setTimestamp(LocalDateTime.now());
  }

  @Test
  void getCurrentPumpStatus_WithData_ReturnsStatus() {
    when(waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(testDevice))
        .thenReturn(Optional.of(testData));

    PumpStatus status = pumpService.getCurrentPumpStatus(testDevice);

    assertEquals(PumpStatus.ON, status);
  }

  @Test
  void getCurrentPumpStatus_NoData_ReturnsUnknown() {
    when(waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(testDevice))
        .thenReturn(Optional.empty());

    PumpStatus status = pumpService.getCurrentPumpStatus(testDevice);

    assertEquals(PumpStatus.UNKNOWN, status);
  }

  @Test
  void getLatestData_WithData_ReturnsData() {
    when(waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(testDevice))
        .thenReturn(Optional.of(testData));

    Optional<WaterLevelData> result = pumpService.getLatestData(testDevice);

    assertTrue(result.isPresent());
    assertEquals(PumpStatus.ON, result.get().getPumpStatus());
  }

  @Test
  void getLatestData_NoData_ReturnsEmpty() {
    when(waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(testDevice))
        .thenReturn(Optional.empty());

    Optional<WaterLevelData> result = pumpService.getLatestData(testDevice);

    assertFalse(result.isPresent());
  }
}
