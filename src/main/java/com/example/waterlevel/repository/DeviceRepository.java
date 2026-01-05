package com.example.waterlevel.repository;

import com.example.waterlevel.entity.Device;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  Page<Device> findAll(Pageable pageable);

  /**
   * Finds all devices with admin eagerly loaded to prevent N+1 queries. EntityGraph ensures admin
   * is fetched in the same query using JOIN.
   */
  @EntityGraph(attributePaths = {"admin"})
  @Override
  List<Device> findAll();

  boolean existsByAdminId(Long adminId);
}
