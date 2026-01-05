package com.example.waterlevel.service;

import com.example.waterlevel.entity.PumpStatus;

/**
 * Interface for sending real-time updates to frontend via WebSocket.
 *
 * <p>Defines the contract for broadcasting sensor data updates and device status changes to
 * connected WebSocket clients.
 */
public interface WebSocketService {

  /**
   * Sends sensor data update to frontend.
   *
   * @param deviceId the device ID
   * @param waterLevel the current water level
   * @param pumpStatus the current pump status
   * @param timestamp the timestamp of the reading
   */
  void sendSensorUpdate(Long deviceId, Double waterLevel, PumpStatus pumpStatus, String timestamp);

  /**
   * Sends threshold update confirmation to frontend.
   *
   * @param deviceId the device ID
   * @param minThreshold the new minimum threshold
   * @param maxThreshold the new maximum threshold
   */
  void sendThresholdUpdateConfirmation(Long deviceId, Double minThreshold, Double maxThreshold);
}
