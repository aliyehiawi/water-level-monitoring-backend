package com.example.waterlevel.service;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for pump status operations. */
@Service
public class PumpService {

  private final WaterLevelDataRepository waterLevelDataRepository;

  @Autowired
  public PumpService(final WaterLevelDataRepository waterLevelDataRepository) {
    this.waterLevelDataRepository = waterLevelDataRepository;
  }

  /**
   * Gets the current pump status for a device.
   *
   * @param device the device
   * @return the latest pump status, or "UNKNOWN" if no data available
   */
  public String getCurrentPumpStatus(final Device device) {
    Optional<WaterLevelData> latestData =
        waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(device);
    return latestData.map(WaterLevelData::getPumpStatus).orElse("UNKNOWN");
  }

  /**
   * Gets the latest water level data for a device.
   *
   * @param device the device
   * @return the latest water level data, or empty if none available
   */
  public Optional<WaterLevelData> getLatestData(final Device device) {
    return waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(device);
  }
}
