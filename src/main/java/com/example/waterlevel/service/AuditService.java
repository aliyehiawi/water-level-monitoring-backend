package com.example.waterlevel.service;

/**
 * Interface for audit logging of critical operations.
 *
 * <p>Defines the contract for logging all critical operations to the audit log for compliance and
 * security tracking.
 */
public interface AuditService {

  /**
   * Logs device registration event.
   *
   * @param adminId the ID of the admin who registered the device
   * @param deviceId the ID of the registered device
   * @param deviceName the name of the device
   */
  void logDeviceRegistration(Long adminId, Long deviceId, String deviceName);

  /**
   * Logs device deletion event.
   *
   * @param adminId the ID of the admin who deleted the device
   * @param deviceId the ID of the deleted device
   */
  void logDeviceDeletion(Long adminId, Long deviceId);

  /**
   * Logs threshold update event.
   *
   * @param adminId the ID of the admin who updated the thresholds
   * @param deviceId the ID of the device
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   */
  void logThresholdUpdate(Long adminId, Long deviceId, Double minThreshold, Double maxThreshold);

  /**
   * Logs pump start event.
   *
   * @param adminId the ID of the admin who started the pump
   * @param deviceId the ID of the device
   */
  void logPumpStart(Long adminId, Long deviceId);

  /**
   * Logs user promotion event.
   *
   * @param adminId the ID of the admin who promoted the user
   * @param userId the ID of the promoted user
   * @param username the username of the promoted user
   */
  void logUserPromotion(Long adminId, Long userId, String username);

  /**
   * Logs user deletion event.
   *
   * @param adminId the ID of the admin who deleted the user
   * @param userId the ID of the deleted user
   */
  void logUserDeletion(Long adminId, Long userId);

  /**
   * Logs user registration event.
   *
   * @param userId the ID of the registered user
   * @param username the username of the registered user
   */
  void logUserRegistration(Long userId, String username);
}
