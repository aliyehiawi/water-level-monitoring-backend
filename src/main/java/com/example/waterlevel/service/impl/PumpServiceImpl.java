package com.example.waterlevel.service.impl;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.PumpStatus;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.PumpService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for pump status operations. */
@Service
public class PumpServiceImpl implements PumpService {

  private final WaterLevelDataRepository waterLevelDataRepository;

  public PumpServiceImpl(final WaterLevelDataRepository waterLevelDataRepository) {
    this.waterLevelDataRepository = waterLevelDataRepository;
  }

  /**
   * Gets the current pump status for a device.
   *
   * <p>Retrieves the most recent pump status from sensor data. Returns UNKNOWN if no data is
   * available.
   *
   * @param device the device
   * @return the latest pump status, or UNKNOWN if no data available
   */
  @Override
  @Transactional(readOnly = true)
  public PumpStatus getCurrentPumpStatus(final Device device) {
    Optional<WaterLevelData> latestData =
        waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(device);
    return latestData.map(WaterLevelData::getPumpStatus).orElse(PumpStatus.UNKNOWN);
  }

  /**
   * Gets the latest water level data for a device.
   *
   * <p>Retrieves the most recent sensor reading for the specified device.
   *
   * @param device the device
   * @return the latest water level data, or empty if none available
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<WaterLevelData> getLatestData(final Device device) {
    return waterLevelDataRepository.findFirstByDeviceOrderByTimestampDesc(device);
  }
}
