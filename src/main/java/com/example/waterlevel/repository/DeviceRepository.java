package com.example.waterlevel.repository;

import com.example.waterlevel.entity.Device;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

  Optional<Device> findByDeviceKey(String deviceKey);

  boolean existsByDeviceKey(String deviceKey);
}
