package com.example.waterlevel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "water_level_data",
    indexes = {
      @Index(name = "idx_water_level_data_device_id", columnList = "device_id"),
      @Index(name = "idx_water_level_data_timestamp", columnList = "timestamp"),
      @Index(name = "idx_water_level_data_device_timestamp", columnList = "device_id,timestamp")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaterLevelData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "device_id", nullable = false)
  private Device device;

  @Column(name = "water_level", nullable = false, precision = 5, scale = 2)
  private BigDecimal waterLevel;

  @Enumerated(EnumType.STRING)
  @Column(name = "pump_status", nullable = false, length = 10)
  private PumpStatus pumpStatus;

  @Column(nullable = false, updatable = false)
  private LocalDateTime timestamp;

  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }
}
