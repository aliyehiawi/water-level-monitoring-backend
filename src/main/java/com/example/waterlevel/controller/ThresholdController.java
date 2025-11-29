package com.example.waterlevel.controller;

import com.example.waterlevel.dto.ThresholdResponse;
import com.example.waterlevel.dto.ThresholdUpdateRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.MqttService;
import com.example.waterlevel.service.WebSocketService;
import com.example.waterlevel.util.SecurityUtil;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for threshold management endpoints (admin only). */
@RestController
@RequestMapping("/api/devices/{deviceId}/thresholds")
@PreAuthorize("hasRole('ADMIN')")
public class ThresholdController {

  private final DeviceService deviceService;
  private final MqttService mqttService;
  private final UserRepository userRepository;
  private final WebSocketService webSocketService;

  @Autowired
  public ThresholdController(
      final DeviceService deviceService,
      final MqttService mqttService,
      final UserRepository userRepository,
      final WebSocketService webSocketService) {
    this.deviceService = deviceService;
    this.mqttService = mqttService;
    this.userRepository = userRepository;
    this.webSocketService = webSocketService;
  }

  /**
   * Gets current thresholds for a device.
   *
   * @param deviceId the device ID
   * @return the current thresholds
   */
  @GetMapping
  public ResponseEntity<ThresholdResponse> getThresholds(@PathVariable final Long deviceId) {
    try {
      String username = SecurityUtil.getCurrentUsername();
      User admin =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

      Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

      ThresholdResponse response =
          new ThresholdResponse(device.getMinThreshold(), device.getMaxThreshold());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Updates thresholds for a device.
   *
   * @param deviceId the device ID
   * @param request the threshold update request
   * @return the updated thresholds
   */
  @PutMapping
  @Transactional
  public ResponseEntity<ThresholdResponse> updateThresholds(
      @PathVariable final Long deviceId, @Valid @RequestBody final ThresholdUpdateRequest request) {
    try {
      String username = SecurityUtil.getCurrentUsername();
      User admin =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

      // Validate ownership
      Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

      // Validate thresholds
      if (request.getMinThreshold() >= request.getMaxThreshold()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }

      // Update device
      device.setMinThreshold(BigDecimal.valueOf(request.getMinThreshold()));
      device.setMaxThreshold(BigDecimal.valueOf(request.getMaxThreshold()));

      // Publish to MQTT for hardware
      boolean mqttSuccess =
          mqttService.publishThresholdUpdate(
              device.getDeviceKey(),
              request.getMinThreshold(),
              request.getMaxThreshold(),
              admin.getId());

      if (!mqttSuccess) {
        // Log warning but continue - device will get update on next sensor data submission
        // or can poll for threshold updates
      }

      // Broadcast to frontend via WebSocket
      webSocketService.sendThresholdUpdateConfirmation(
          deviceId, request.getMinThreshold(), request.getMaxThreshold());

      ThresholdResponse response =
          new ThresholdResponse(device.getMinThreshold(), device.getMaxThreshold());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
