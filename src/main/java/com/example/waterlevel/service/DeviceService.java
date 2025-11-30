package com.example.waterlevel.service;

import com.example.waterlevel.dto.DeviceRegisterRequest;
import com.example.waterlevel.entity.Device;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for device management operations.
 *
 * <p>Defines the contract for device registration, retrieval, and management services.
 */
public interface DeviceService {

  /**
   * Registers a new device for the given admin user.
   *
   * @param request the device registration request
   * @param adminId the ID of the admin user registering the device
   * @return the created device
   */
  Device registerDevice(DeviceRegisterRequest request, Long adminId);

  /**
   * Gets all devices.
   *
   * @return list of all devices
   */
  List<Device> getAllDevices();

  /**
   * Gets all devices with pagination.
   *
   * @param pageable pagination information
   * @return paginated list of devices
   */
  Page<Device> getAllDevices(Pageable pageable);

  /**
   * Gets a device by ID.
   *
   * @param deviceId the device ID
   * @return the device
   */
  Device getDeviceById(Long deviceId);

  /**
   * Validates that the device belongs to the given admin.
   *
   * @param deviceId the device ID
   * @param adminId the admin user ID
   * @return the device if valid
   * @throws IllegalArgumentException if device not found or doesn't belong to admin
   */
  Device validateDeviceOwnership(Long deviceId, Long adminId);

  /**
   * Updates thresholds for a device.
   *
   * @param device the device to update (must be a managed entity)
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   * @return the updated device
   */
  Device updateThresholds(Device device, BigDecimal minThreshold, BigDecimal maxThreshold);

  /**
   * Deletes a device.
   *
   * @param deviceId the device ID
   */
  void deleteDevice(Long deviceId);
}
