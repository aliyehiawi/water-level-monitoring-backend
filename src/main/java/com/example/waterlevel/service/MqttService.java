package com.example.waterlevel.service;

/**
 * Interface for publishing MQTT messages to hardware devices.
 *
 * <p>Defines the contract for publishing pump commands and threshold updates to devices via MQTT
 * with retry logic.
 */
public interface MqttService {

  /**
   * Publishes a pump start command to the specified device.
   *
   * @param deviceKey the device key (UUID) of the target device
   * @param initiatedBy the user ID who initiated the command
   * @return true if message was sent successfully, false otherwise
   */
  boolean publishPumpStartCommand(String deviceKey, Long initiatedBy);

  /**
   * Publishes threshold update to the specified device.
   *
   * @param deviceKey the device key (UUID) of the target device
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   * @param updatedBy the user ID who updated the thresholds
   * @return true if message was sent successfully, false otherwise
   */
  boolean publishThresholdUpdate(
      String deviceKey, Double minThreshold, Double maxThreshold, Long updatedBy);
}
