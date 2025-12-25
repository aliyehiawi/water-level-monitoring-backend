package com.example.waterlevel.service.impl;

import com.example.waterlevel.constants.ApplicationConstants;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.WaterLevelDataRepository;
import com.example.waterlevel.service.WaterLevelDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaterLevelDataServiceImpl implements WaterLevelDataService {

  private final WaterLevelDataRepository waterLevelDataRepository;
  private final DeviceRepository deviceRepository;

  public WaterLevelDataServiceImpl(
      final WaterLevelDataRepository waterLevelDataRepository,
      final DeviceRepository deviceRepository) {
    this.waterLevelDataRepository = waterLevelDataRepository;
    this.deviceRepository = deviceRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<WaterLevelData> getWaterLevelDataForDevice(
      final Long deviceId, final Pageable pageable) {
    if (deviceId == null) {
      throw new IllegalArgumentException(ApplicationConstants.DEVICE_NOT_FOUND_MESSAGE);
    }
    if (!deviceRepository.existsById(deviceId)) {
      throw new IllegalArgumentException(ApplicationConstants.DEVICE_NOT_FOUND_MESSAGE);
    }
    return waterLevelDataRepository.findByDevice_Id(deviceId, pageable);
  }
}
