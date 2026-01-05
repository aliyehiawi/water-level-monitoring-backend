package com.example.waterlevel.service;

import com.example.waterlevel.entity.WaterLevelData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WaterLevelDataService {
  /**
   * Returns paginated historical readings for an existing device.
   *
   * <p>Implementation validates device existence to avoid returning an empty page that could be
   * mistaken as "no history yet".
   *
   * @throws IllegalArgumentException if {@code deviceId} is null or the device does not exist
   */
  Page<WaterLevelData> getWaterLevelDataForDevice(Long deviceId, Pageable pageable);
}
