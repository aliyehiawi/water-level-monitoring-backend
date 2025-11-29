package com.example.waterlevel.controller;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.dto.DeviceResponse;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.DeviceService;
import com.example.waterlevel.util.SecurityUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for device management endpoints (admin only). */
@RestController
@RequestMapping("/api/devices")
@PreAuthorize("hasRole('ADMIN')")
public class DeviceController {

  private final DeviceService deviceService;
  private final UserRepository userRepository;

  @Autowired
  public DeviceController(final DeviceService deviceService, final UserRepository userRepository) {
    this.deviceService = deviceService;
    this.userRepository = userRepository;
  }

  /**
   * Registers a new device.
   *
   * @param request the device registration request
   * @return the created device with device key
   */
  @PostMapping("/register")
  public ResponseEntity<DeviceResponse> registerDevice(
      @Valid @RequestBody final DeviceRegisterRequest request) {
    try {
      String username = SecurityUtil.getCurrentUsername();
      User admin =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

      Device device = deviceService.registerDevice(request, admin.getId());

      DeviceResponse response = mapToResponse(device);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  /**
   * Gets all devices.
   *
   * @return list of all devices
   */
  @GetMapping
  public ResponseEntity<List<DeviceResponse>> getAllDevices() {
    List<Device> devices = deviceService.getAllDevices();
    List<DeviceResponse> responses =
        devices.stream().map(this::mapToResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Deletes a device.
   *
   * @param id the device ID
   * @return no content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDevice(@PathVariable final Long id) {
    try {
      String username = SecurityUtil.getCurrentUsername();
      User admin =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

      // Validate ownership
      deviceService.validateDeviceOwnership(id, admin.getId());

      deviceService.deleteDevice(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  private DeviceResponse mapToResponse(final Device device) {
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
