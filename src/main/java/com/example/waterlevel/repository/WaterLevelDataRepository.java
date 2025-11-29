package com.example.waterlevel.repository;

import com.example.waterlevel.entity.Device;
import com.example.waterlevel.entity.WaterLevelData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WaterLevelDataRepository extends JpaRepository<WaterLevelData, Long> {

  List<WaterLevelData> findByDeviceOrderByTimestampDesc(Device device, Pageable pageable);

  @Query(
      "SELECT w FROM WaterLevelData w WHERE w.device.id = :deviceId " + "ORDER BY w.timestamp DESC")
  List<WaterLevelData> findLatestByDeviceId(@Param("deviceId") Long deviceId, Pageable pageable);

  @Query(
      "SELECT w FROM WaterLevelData w WHERE w.device.id = :deviceId "
          + "AND w.timestamp BETWEEN :from AND :to "
          + "ORDER BY w.timestamp DESC")
  List<WaterLevelData> findByDeviceIdAndTimestampBetween(
      @Param("deviceId") Long deviceId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  Optional<WaterLevelData> findFirstByDeviceOrderByTimestampDesc(Device device);
}
