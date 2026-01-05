package com.example.waterlevel.controller;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.dto.DeviceResponse;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for device management endpoints. */
@RestController
@RequestMapping("/devices")
@Tag(name = "Device Management", description = "Device management operations")
public class DeviceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceController.class);

  private final DeviceService deviceService;
  private final UserService userService;
  private final AuditService auditService;

  public DeviceController(
      final DeviceService deviceService,
      final UserService userService,
      final AuditService auditService) {
    this.deviceService = deviceService;
    this.userService = userService;
    this.auditService = auditService;
  }

  /**
   * Registers a new device.
   *
   * @param request the device registration request
   * @return the created device with device key
   */
  @Operation(
      summary = "Register a new device",
      description = "Creates a new water level monitoring device")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Device registered successfully"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data or threshold validation failed")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/register")
  public ResponseEntity<DeviceResponse> registerDevice(
      @Valid @RequestBody final DeviceRegisterRequest request) {
    LOGGER.info("Device registration request: {}", request.getName());
    User admin = userService.getCurrentUser();

    Device device = deviceService.registerDevice(request, admin.getId());

    DeviceResponse response = mapToResponse(device);
    LOGGER.info(
        "Device registered successfully by admin {}: deviceId={}",
        admin.getUsername(),
        device.getId());
    auditService.logDeviceRegistration(admin.getId(), device.getId(), device.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Gets all devices with pagination.
   *
   * @param page page number (default: 0)
   * @param size page size (default: 20)
   * @return paginated list of devices
   */
  @Operation(
      summary = "Get all devices",
      description = "Retrieves a paginated list of all devices (available to authenticated users)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @GetMapping
  public ResponseEntity<Page<DeviceResponse>> getAllDevices(
      @Parameter(description = "Page number (0-indexed)", example = "0")
          @RequestParam(defaultValue = "0")
          @Min(value = 0, message = "Page number must be >= 0")
          final int page,
      @Parameter(description = "Page size", example = "20")
          @RequestParam(defaultValue = "20")
          @Min(value = 1, message = "Page size must be >= 1")
          @Max(value = 100, message = "Page size must be <= 100")
          final int size) {
    LOGGER.debug("Get all devices requested: page={}, size={}", page, size);
    Pageable pageable = PageRequest.of(page, size);
    Page<Device> devices = deviceService.getAllDevices(pageable);
    Page<DeviceResponse> responses = devices.map(this::mapToResponse);
    LOGGER.debug("Returning {} devices (page {})", responses.getNumberOfElements(), page);
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a device by ID.
   *
   * @param id the device ID
   * @return the device details
   */
  @Operation(
      summary = "Get device details",
      description = "Retrieves details for a specific device (available to authenticated users)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Device details retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Device not found"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @GetMapping("/{id}")
  public ResponseEntity<DeviceResponse> getDeviceById(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long id) {
    LOGGER.debug("Get device requested: deviceId={}", id);
    Device device = deviceService.getDeviceById(id);
    return ResponseEntity.ok(mapToResponse(device));
  }

  /**
   * Deletes a device.
   *
   * @param id the device ID
   * @return no content
   */
  @Operation(
      summary = "Delete a device",
      description = "Deletes a device by ID (admin must own the device)")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Device not found or access denied")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDevice(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long id) {
    LOGGER.info("Device deletion request: deviceId={}", id);
    User admin = userService.getCurrentUser();

    deviceService.validateDeviceOwnership(id, admin.getId());

    deviceService.deleteDevice(id);
    LOGGER.info("Device deleted successfully by admin {}: deviceId={}", admin.getUsername(), id);
    auditService.logDeviceDeletion(admin.getId(), id);
    return ResponseEntity.noContent().build();
  }

  private DeviceResponse mapToResponse(final Device device) {
    if (device.getAdmin() == null) {
      throw new IllegalStateException("Device configuration error");
    }
    return new DeviceResponse(
        device.getId(),
        device.getName(),
        device.getDeviceKey(),
        device.getMinThreshold(),
        device.getMaxThreshold(),
        device.getAdmin().getId(),
        device.getAdmin().getUsername(),
        device.getCreatedAt(),
        device.getUpdatedAt());
  }
}
