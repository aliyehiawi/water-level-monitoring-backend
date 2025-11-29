package com.example.waterlevel.controller;

import com.example.waterlevel.dto.PumpStatusResponse;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.MqttService;
import com.example.waterlevel.service.PumpService;
import com.example.waterlevel.util.SecurityUtil;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for pump control endpoints (admin only). */
@RestController
@RequestMapping("/api/devices/{deviceId}/pump")
@PreAuthorize("hasRole('ADMIN')")
public class PumpController {

  private final DeviceService deviceService;
  private final MqttService mqttService;
  private final PumpService pumpService;
  private final UserRepository userRepository;

  @Autowired
  public PumpController(
      final DeviceService deviceService,
      final MqttService mqttService,
      final PumpService pumpService,
      final UserRepository userRepository) {
    this.deviceService = deviceService;
    this.mqttService = mqttService;
    this.pumpService = pumpService;
    this.userRepository = userRepository;
  }

  /**
   * Starts the pump manually for a device.
   *
   * @param deviceId the device ID
   * @return success response
   */
  @PostMapping("/start")
  public ResponseEntity<Void> startPump(@PathVariable final Long deviceId) {
    try {
      String username = SecurityUtil.getCurrentUsername();
      User admin =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

      // Validate ownership
      Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

      // Publish MQTT command to start pump
      boolean mqttSuccess =
          mqttService.publishPumpStartCommand(device.getDeviceKey(), admin.getId());

      if (!mqttSuccess) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Gets the current pump status for a device.
   *
   * @param deviceId the device ID
   * @return the pump status
   */
  @GetMapping("/status")
  public ResponseEntity<PumpStatusResponse> getPumpStatus(@PathVariable final Long deviceId) {
    try {
      String username = SecurityUtil.getCurrentUsername();
      User admin =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

      // Validate ownership
      Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

      // Get latest pump status
      String pumpStatus = pumpService.getCurrentPumpStatus(device);
      LocalDateTime lastUpdate = null;

      java.util.Optional<WaterLevelData> latestData = pumpService.getLatestData(device);
      if (latestData.isPresent()) {
        lastUpdate = latestData.get().getTimestamp();
      }

      PumpStatusResponse response = new PumpStatusResponse(pumpStatus, lastUpdate);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
