package com.example.waterlevel.service;

import org.springframework.messaging.Message;

/**
 * Interface for processing sensor data received from hardware via MQTT.
 *
 * <p>Defines the contract for receiving MQTT messages, validating device keys, storing data, and
 * broadcasting to frontend.
 */
public interface SensorDataService {

  /**
   * Processes incoming MQTT sensor data messages.
   *
   * @param message the MQTT message containing sensor data
   */
  void processSensorData(Message<byte[]> message);
}
