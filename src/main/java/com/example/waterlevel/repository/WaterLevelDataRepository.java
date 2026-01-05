package com.example.waterlevel.repository;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.WaterLevelData;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WaterLevelDataRepository extends JpaRepository<WaterLevelData, Long> {

  Page<WaterLevelData> findByDevice_Id(Long deviceId, Pageable pageable);

  Optional<WaterLevelData> findFirstByDeviceOrderByTimestampDesc(Device device);

  @Modifying
  @Transactional
  @Query("DELETE FROM WaterLevelData w WHERE w.device = :device")
  void deleteByDevice(@Param("device") Device device);
}
