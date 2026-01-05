package com.example.waterlevel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.Role;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.impl.DeviceServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

  @Mock private DeviceRepository deviceRepository;
  @Mock private UserRepository userRepository;
  @Mock private WaterLevelDataRepository waterLevelDataRepository;

  @InjectMocks private DeviceServiceImpl deviceService;

  private User testAdmin;
  private DeviceRegisterRequest registerRequest;
  private Device testDevice;

  @BeforeEach
  void setUp() {
    testAdmin = new User();
    testAdmin.setId(1L);
    testAdmin.setUsername("admin");
    testAdmin.setRole(Role.ADMIN);

    registerRequest = new DeviceRegisterRequest();
    registerRequest.setName("Test Device");
    registerRequest.setMinThreshold(10.0);
    registerRequest.setMaxThreshold(90.0);

    testDevice = new Device();
    testDevice.setId(1L);
    testDevice.setName("Test Device");
    testDevice.setDeviceKey("test-device-key");
    testDevice.setMinThreshold(BigDecimal.valueOf(10.0));
    testDevice.setMaxThreshold(BigDecimal.valueOf(90.0));
    testDevice.setAdmin(testAdmin);
  }

  @Test
  void registerDevice_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
    when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

    Device result = deviceService.registerDevice(registerRequest, 1L);

    assertNotNull(result);
    assertEquals("Test Device", result.getName());
    verify(deviceRepository).save(any(Device.class));
  }

  @Test
  void registerDevice_InvalidThresholds_DoesNotThrow() {
    registerRequest.setMinThreshold(90.0);
    registerRequest.setMaxThreshold(10.0);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
    when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

    Device result = deviceService.registerDevice(registerRequest, 1L);

    assertNotNull(result);
    verify(deviceRepository).save(any(Device.class));
  }

  @Test
  void registerDevice_AdminNotFound_ThrowsException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class, () -> deviceService.registerDevice(registerRequest, 1L));
    verify(deviceRepository, never()).save(any(Device.class));
  }

  @Test
  void getDeviceById_Success() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

    Device result = deviceService.getDeviceById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
  }

  @Test
  void getDeviceById_NotFound_ThrowsException() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> deviceService.getDeviceById(1L));
  }

  @Test
  void validateDeviceOwnership_Success() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

    Device result = deviceService.validateDeviceOwnership(1L, 1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
  }

  @Test
  void validateDeviceOwnership_WrongOwner_ThrowsException() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

    assertThrows(
        IllegalArgumentException.class, () -> deviceService.validateDeviceOwnership(1L, 2L));
  }

  @Test
  void deleteDevice_Success() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
    doNothing().when(waterLevelDataRepository).deleteByDevice(any(Device.class));
    doNothing().when(deviceRepository).deleteById(1L);

    deviceService.deleteDevice(1L);

    verify(waterLevelDataRepository).deleteByDevice(any(Device.class));
    verify(deviceRepository).deleteById(1L);
  }

  @Test
  void deleteDevice_NotFound_ThrowsException() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> deviceService.deleteDevice(1L));
    verify(waterLevelDataRepository, never()).deleteByDevice(any());
    verify(deviceRepository, never()).deleteById(any());
  }
}
