package com.example.waterlevel.service;

import com.example.waterlevel.entity.WaterLevelData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WaterLevelDataService {
  Page<WaterLevelData> getWaterLevelDataForDevice(Long deviceId, Pageable pageable);
}
