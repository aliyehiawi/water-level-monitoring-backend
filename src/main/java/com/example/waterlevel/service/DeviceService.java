package com.example.waterlevel.service;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for device management operations. */
@Service
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final UserRepository userRepository;

  @Autowired
  public DeviceService(
      final DeviceRepository deviceRepository, final UserRepository userRepository) {
    this.deviceRepository = deviceRepository;
    this.userRepository = userRepository;
  }

  /**
   * Registers a new device for the given admin user.
   *
   * @param request the device registration request
   * @param adminId the ID of the admin user registering the device
   * @return the created device
   */
  @Transactional
  public Device registerDevice(final DeviceRegisterRequest request, final Long adminId) {
    User admin =
        userRepository
            .findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + adminId));

    // Validate thresholds
    if (request.getMinThreshold() >= request.getMaxThreshold()) {
      throw new IllegalArgumentException("Minimum threshold must be less than maximum threshold");
    }

    // Generate unique device key
    String deviceKey = UUID.randomUUID().toString();

    // Create device
    Device device = new Device();
    device.setName(request.getName());
    device.setDeviceKey(deviceKey);
    device.setMinThreshold(BigDecimal.valueOf(request.getMinThreshold()));
    device.setMaxThreshold(BigDecimal.valueOf(request.getMaxThreshold()));
    device.setAdmin(admin);

    return deviceRepository.save(device);
  }

  /**
   * Gets all devices.
   *
   * @return list of all devices
   */
  public List<Device> getAllDevices() {
    return deviceRepository.findAll();
  }

  /**
   * Gets a device by ID.
   *
   * @param deviceId the device ID
   * @return the device
   */
  public Device getDeviceById(final Long deviceId) {
    return deviceRepository
        .findById(deviceId)
        .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
  }

  /**
   * Validates that the device belongs to the given admin.
   *
   * @param deviceId the device ID
   * @param adminId the admin user ID
   * @return the device if valid
   * @throws IllegalArgumentException if device not found or doesn't belong to admin
   */
  public Device validateDeviceOwnership(final Long deviceId, final Long adminId) {
    Device device = getDeviceById(deviceId);
    if (!device.getAdmin().getId().equals(adminId)) {
      throw new IllegalArgumentException(
          "Device does not belong to the current user or access denied");
    }
    return device;
  }

  /**
   * Deletes a device.
   *
   * @param deviceId the device ID
   */
  @Transactional
  public void deleteDevice(final Long deviceId) {
    if (!deviceRepository.existsById(deviceId)) {
      throw new IllegalArgumentException("Device not found: " + deviceId);
    }
    deviceRepository.deleteById(deviceId);
  }
}
