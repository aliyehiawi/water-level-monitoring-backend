package com.example.waterlevel.service.impl;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.DeviceService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for device management operations. */
@Service
public class DeviceServiceImpl implements DeviceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceImpl.class);

  private final DeviceRepository deviceRepository;
  private final UserRepository userRepository;
  private final WaterLevelDataRepository waterLevelDataRepository;

  public DeviceServiceImpl(
      final DeviceRepository deviceRepository,
      final UserRepository userRepository,
      final WaterLevelDataRepository waterLevelDataRepository) {
    this.deviceRepository = deviceRepository;
    this.userRepository = userRepository;
    this.waterLevelDataRepository = waterLevelDataRepository;
  }

  /**
   * Registers a new device for the given admin user.
   *
   * @param request the device registration request
   * @param adminId the ID of the admin user registering the device
   * @return the created device
   */
  @Override
  @Transactional
  public Device registerDevice(final DeviceRegisterRequest request, final Long adminId) {
    LOGGER.info("Registering device '{}' for admin ID: {}", request.getName(), adminId);
    User admin =
        userRepository
            .findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Generate unique device key
    String deviceKey = UUID.randomUUID().toString();

    // Create device
    Device device = new Device();
    device.setName(request.getName());
    device.setDeviceKey(deviceKey);
    device.setMinThreshold(BigDecimal.valueOf(request.getMinThreshold()));
    device.setMaxThreshold(BigDecimal.valueOf(request.getMaxThreshold()));
    device.setAdmin(admin);

    Device savedDevice = deviceRepository.save(device);
    LOGGER.info(
        "Device registered successfully: {} (ID: {}, Key: {})",
        savedDevice.getName(),
        savedDevice.getId(),
        savedDevice.getDeviceKey());
    return savedDevice;
  }

  /**
   * Gets all devices.
   *
   * @return list of all devices
   */
  @Override
  @Transactional(readOnly = true)
  public List<Device> getAllDevices() {
    return deviceRepository.findAll();
  }

  /**
   * Gets all devices with pagination.
   *
   * <p>Uses EntityGraph to eagerly fetch admin relationships to avoid N+1 queries.
   *
   * @param pageable pagination information
   * @return paginated list of devices
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Device> getAllDevices(final Pageable pageable) {
    // Note: findAll with EntityGraph requires custom query or specification
    // For now, using findAll and relying on batch fetching configuration
    return deviceRepository.findAll(pageable);
  }

  /**
   * Gets a device by ID.
   *
   * @param deviceId the device ID
   * @return the device
   */
  @Override
  @Transactional(readOnly = true)
  public Device getDeviceById(final Long deviceId) {
    return deviceRepository
        .findById(deviceId)
        .orElseThrow(() -> new IllegalArgumentException("Device not found"));
  }

  /**
   * Validates that the device belongs to the given admin.
   *
   * @param deviceId the device ID
   * @param adminId the admin user ID
   * @return the device if valid
   * @throws IllegalArgumentException if device not found or doesn't belong to admin
   */
  @Override
  @Transactional(readOnly = true)
  public Device validateDeviceOwnership(final Long deviceId, final Long adminId) {
    LOGGER.debug("Validating device ownership: deviceId={}, adminId={}", deviceId, adminId);
    Device device = getDeviceById(deviceId);
    if (device.getAdmin() == null || !device.getAdmin().getId().equals(adminId)) {
      LOGGER.warn("Device ownership validation failed: deviceId={}, adminId={}", deviceId, adminId);
      throw new IllegalArgumentException(
          "Device does not belong to the current user or access denied");
    }
    return device;
  }

  /**
   * Updates thresholds for a device.
   *
   * @param device the device to update (must be a managed entity)
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   * @return the updated device
   */
  @Override
  @Transactional
  public Device updateThresholds(
      final Device device, final BigDecimal minThreshold, final BigDecimal maxThreshold) {
    LOGGER.info(
        "Updating thresholds for device ID: {}, min: {}, max: {}",
        device.getId(),
        minThreshold,
        maxThreshold);
    device.setMinThreshold(minThreshold);
    device.setMaxThreshold(maxThreshold);
    Device savedDevice = deviceRepository.save(device);
    LOGGER.info("Thresholds updated successfully for device ID: {}", device.getId());
    return savedDevice;
  }

  /**
   * Deletes a device.
   *
   * @param deviceId the device ID
   */
  @Override
  @Transactional
  public void deleteDevice(final Long deviceId) {
    LOGGER.info("Deleting device ID: {}", deviceId);
    Device device = getDeviceById(deviceId);

    // Delete all associated water level data first
    waterLevelDataRepository.deleteByDevice(device);
    LOGGER.debug("Deleted water level data for device ID: {}", deviceId);

    deviceRepository.deleteById(deviceId);
    LOGGER.info("Device deleted successfully: {}", deviceId);
  }
}
