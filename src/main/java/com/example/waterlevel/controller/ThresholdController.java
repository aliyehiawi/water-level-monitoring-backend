package com.example.waterlevel.controller;

import com.example.waterlevel.dto.ThresholdResponse;
import com.example.waterlevel.dto.ThresholdUpdateRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.MqttService;
import com.example.waterlevel.service.WebSocketService;
import com.example.waterlevel.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
@Tag(name = "Threshold Management", description = "Admin operations for device thresholds")
public class ThresholdController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThresholdController.class);

  private final DeviceService deviceService;
  private final MqttService mqttService;
  private final UserRepository userRepository;
  private final WebSocketService webSocketService;
  private final AuditService auditService;

  public ThresholdController(
      final DeviceService deviceService,
      final MqttService mqttService,
      final UserRepository userRepository,
      final WebSocketService webSocketService,
      final AuditService auditService) {
    this.deviceService = deviceService;
    this.mqttService = mqttService;
    this.userRepository = userRepository;
    this.webSocketService = webSocketService;
    this.auditService = auditService;
  }

  /**
   * Gets current thresholds for a device.
   *
   * @param deviceId the device ID
   * @return the current thresholds
   */
  @Operation(
      summary = "Get device thresholds",
      description = "Retrieves the current min and max thresholds for a device")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Thresholds retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Device not found or access denied")
  })
  @GetMapping
  public ResponseEntity<ThresholdResponse> getThresholds(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long deviceId) {
    LOGGER.debug("Get thresholds request for deviceId: {}", deviceId);
    String username = SecurityUtil.getCurrentUsername();
    User admin =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

    ThresholdResponse response =
        new ThresholdResponse(device.getMinThreshold(), device.getMaxThreshold());
    return ResponseEntity.ok(response);
  }

  /**
   * Updates thresholds for a device.
   *
   * @param deviceId the device ID
   * @param request the threshold update request
   * @return the updated thresholds
   */
  @Operation(
      summary = "Update device thresholds",
      description =
          "Updates the min and max thresholds for a device and publishes to hardware via MQTT")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Thresholds updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid thresholds or validation failed")
  })
  @PutMapping
  public ResponseEntity<ThresholdResponse> updateThresholds(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long deviceId,
      @Valid @RequestBody final ThresholdUpdateRequest request) {
    LOGGER.info(
        "Threshold update request for deviceId: {}, min: {}, max: {}",
        deviceId,
        request.getMinThreshold(),
        request.getMaxThreshold());
    String username = SecurityUtil.getCurrentUsername();
    User admin =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // Validate ownership and get device (reuse to avoid duplicate query)
    Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

    // Update device thresholds (DTO validation ensures minThreshold < maxThreshold)
    Device updatedDevice =
        deviceService.updateThresholds(
            device,
            BigDecimal.valueOf(request.getMinThreshold()),
            BigDecimal.valueOf(request.getMaxThreshold()));

    // Publish to MQTT for hardware
    boolean mqttSuccess =
        mqttService.publishThresholdUpdate(
            updatedDevice.getDeviceKey(),
            request.getMinThreshold(),
            request.getMaxThreshold(),
            admin.getId());

    if (!mqttSuccess) {
      LOGGER.warn(
          "Failed to publish MQTT threshold update for deviceId: {}, but continuing", deviceId);
      // Device will get update on next sensor data submission or can poll for threshold updates
    } else {
      LOGGER.info("Threshold update published to MQTT successfully for deviceId: {}", deviceId);
    }

    // Broadcast to frontend via WebSocket
    webSocketService.sendThresholdUpdateConfirmation(
        deviceId, request.getMinThreshold(), request.getMaxThreshold());

    ThresholdResponse response =
        new ThresholdResponse(updatedDevice.getMinThreshold(), updatedDevice.getMaxThreshold());
    LOGGER.info("Thresholds updated successfully by admin {} for deviceId: {}", username, deviceId);
    auditService.logThresholdUpdate(
        admin.getId(), deviceId, request.getMinThreshold(), request.getMaxThreshold());
    return ResponseEntity.ok(response);
  }
}
