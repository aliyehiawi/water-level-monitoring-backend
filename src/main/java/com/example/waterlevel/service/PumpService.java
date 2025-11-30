package com.example.waterlevel.service;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.WaterLevelData;
import java.util.Optional;

/**
 * Interface for pump status operations.
 *
 * <p>Defines the contract for retrieving pump status and water level data.
 */
public interface PumpService {

  /**
   * Gets the current pump status for a device.
   *
   * <p>Retrieves the most recent pump status from sensor data. Returns UNKNOWN if no data is
   * available.
   *
   * @param device the device
   * @return the latest pump status, or UNKNOWN if no data available
   */
  PumpStatus getCurrentPumpStatus(Device device);

  /**
   * Gets the latest water level data for a device.
   *
   * <p>Retrieves the most recent sensor reading for the specified device.
   *
   * @param device the device
   * @return the latest water level data, or empty if none available
   */
  Optional<WaterLevelData> getLatestData(Device device);
}
