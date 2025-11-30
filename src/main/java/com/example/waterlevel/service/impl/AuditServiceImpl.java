package com.example.waterlevel.service.impl;

import com.example.waterlevel.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for audit logging of critical operations.
 *
 * <p>Logs all critical operations to the audit log for compliance and security tracking.
 */
@Service
public class AuditServiceImpl implements AuditService {

  private static final Logger AUDIT_LOGGER =
      LoggerFactory.getLogger("com.example.waterlevel.audit");

  /**
   * Logs device registration event.
   *
   * @param adminId the ID of the admin who registered the device
   * @param deviceId the ID of the registered device
   * @param deviceName the name of the device
   */
  @Override
  public void logDeviceRegistration(
      final Long adminId, final Long deviceId, final String deviceName) {
    AUDIT_LOGGER.info(
        "DEVICE_REGISTERED: adminId={}, deviceId={}, deviceName={}", adminId, deviceId, deviceName);
  }

  /**
   * Logs device deletion event.
   *
   * @param adminId the ID of the admin who deleted the device
   * @param deviceId the ID of the deleted device
   */
  @Override
  public void logDeviceDeletion(final Long adminId, final Long deviceId) {
    AUDIT_LOGGER.info("DEVICE_DELETED: adminId={}, deviceId={}", adminId, deviceId);
  }

  /**
   * Logs threshold update event.
   *
   * @param adminId the ID of the admin who updated the thresholds
   * @param deviceId the ID of the device
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   */
  @Override
  public void logThresholdUpdate(
      final Long adminId,
      final Long deviceId,
      final Double minThreshold,
      final Double maxThreshold) {
    AUDIT_LOGGER.info(
        "THRESHOLD_UPDATED: adminId={}, deviceId={}, minThreshold={}, maxThreshold={}",
        adminId,
        deviceId,
        minThreshold,
        maxThreshold);
  }

  /**
   * Logs pump start event.
   *
   * @param adminId the ID of the admin who started the pump
   * @param deviceId the ID of the device
   */
  @Override
  public void logPumpStart(final Long adminId, final Long deviceId) {
    AUDIT_LOGGER.info("PUMP_STARTED: adminId={}, deviceId={}", adminId, deviceId);
  }

  /**
   * Logs user promotion event.
   *
   * @param adminId the ID of the admin who promoted the user
   * @param userId the ID of the promoted user
   * @param username the username of the promoted user
   */
  @Override
  public void logUserPromotion(final Long adminId, final Long userId, final String username) {
    AUDIT_LOGGER.info(
        "USER_PROMOTED: adminId={}, userId={}, username={}", adminId, userId, username);
  }

  /**
   * Logs user deletion event.
   *
   * @param adminId the ID of the admin who deleted the user
   * @param userId the ID of the deleted user
   */
  @Override
  public void logUserDeletion(final Long adminId, final Long userId) {
    AUDIT_LOGGER.info("USER_DELETED: adminId={}, userId={}", adminId, userId);
  }

  /**
   * Logs user registration event.
   *
   * @param userId the ID of the registered user
   * @param username the username of the registered user
   */
  @Override
  public void logUserRegistration(final Long userId, final String username) {
    AUDIT_LOGGER.info("USER_REGISTERED: userId={}, username={}", userId, username);
  }
}
