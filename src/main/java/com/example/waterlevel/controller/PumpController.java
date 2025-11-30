package com.example.waterlevel.controller;

import com.example.waterlevel.dto.PumpStatusResponse;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.MqttService;
import com.example.waterlevel.service.PumpService;
import com.example.waterlevel.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Pump Control", description = "Admin operations for pump control")
public class PumpController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PumpController.class);

  private final DeviceService deviceService;
  private final MqttService mqttService;
  private final PumpService pumpService;
  private final UserRepository userRepository;
  private final AuditService auditService;

  public PumpController(
      final DeviceService deviceService,
      final MqttService mqttService,
      final PumpService pumpService,
      final UserRepository userRepository,
      final AuditService auditService) {
    this.deviceService = deviceService;
    this.mqttService = mqttService;
    this.pumpService = pumpService;
    this.userRepository = userRepository;
    this.auditService = auditService;
  }

  /**
   * Starts the pump manually for a device.
   *
   * @param deviceId the device ID
   * @return success response
   */
  @Operation(
      summary = "Start pump",
      description = "Sends a manual pump start command to the device via MQTT")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Pump start command sent successfully"),
    @ApiResponse(responseCode = "400", description = "Device not found or access denied"),
    @ApiResponse(responseCode = "500", description = "Failed to send MQTT command")
  })
  @PostMapping("/start")
  public ResponseEntity<Void> startPump(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long deviceId) {
    LOGGER.info("Pump start request for deviceId: {}", deviceId);
    String username = SecurityUtil.getCurrentUsername();
    User admin =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // Validate ownership
    Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

    // Publish MQTT command to start pump
    boolean mqttSuccess = mqttService.publishPumpStartCommand(device.getDeviceKey(), admin.getId());

    if (!mqttSuccess) {
      LOGGER.error("Failed to publish MQTT pump start command for deviceId: {}", deviceId);
      throw new IllegalStateException("Failed to send command to device");
    }

    LOGGER.info(
        "Pump start command sent successfully by admin {} for deviceId: {}", username, deviceId);
    auditService.logPumpStart(admin.getId(), deviceId);
    return ResponseEntity.ok().build();
  }

  /**
   * Gets the current pump status for a device.
   *
   * @param deviceId the device ID
   * @return the pump status
   */
  @Operation(
      summary = "Get pump status",
      description = "Retrieves the current pump status (ON, OFF, or UNKNOWN) for a device")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Pump status retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Device not found or access denied")
  })
  @GetMapping("/status")
  public ResponseEntity<PumpStatusResponse> getPumpStatus(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long deviceId) {
    LOGGER.debug("Get pump status request for deviceId: {}", deviceId);
    String username = SecurityUtil.getCurrentUsername();
    User admin =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // Validate ownership
    Device device = deviceService.validateDeviceOwnership(deviceId, admin.getId());

    // Get latest pump status
    PumpStatus pumpStatus = pumpService.getCurrentPumpStatus(device);
    LocalDateTime lastUpdate =
        pumpService.getLatestData(device).map(WaterLevelData::getTimestamp).orElse(null);

    PumpStatusResponse response = new PumpStatusResponse(pumpStatus, lastUpdate);
    return ResponseEntity.ok(response);
  }
}
