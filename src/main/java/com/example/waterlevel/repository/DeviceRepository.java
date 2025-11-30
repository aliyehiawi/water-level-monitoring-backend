package com.example.waterlevel.repository;

import com.example.waterlevel.entity.Device;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

  /**
   * Finds device by key with admin eagerly loaded to prevent N+1 queries. EntityGraph ensures admin
   * is fetched in the same query using JOIN.
   */
  @EntityGraph(attributePaths = {"admin"})
  Optional<Device> findByDeviceKey(String deviceKey);

  /**
   * Finds all devices with admin eagerly loaded to prevent N+1 queries. EntityGraph ensures admin
   * is fetched in the same query using JOIN.
   */
  @EntityGraph(attributePaths = {"admin"})
  @Override
  org.springframework.data.domain.Page<Device> findAll(
      org.springframework.data.domain.Pageable pageable);

  /**
   * Finds all devices with admin eagerly loaded to prevent N+1 queries. EntityGraph ensures admin
   * is fetched in the same query using JOIN.
   */
  @EntityGraph(attributePaths = {"admin"})
  @Override
  java.util.List<Device> findAll();

  boolean existsByDeviceKey(String deviceKey);

  boolean existsByAdminId(Long adminId);
}
