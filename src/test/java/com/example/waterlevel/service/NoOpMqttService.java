package com.example.waterlevel.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of MqttService for tests when MQTT is disabled.
 *
 * <p>This allows tests to run without requiring a real MQTT broker or MQTT configuration. All
 * methods return success without actually sending any MQTT messages.
 */
@Service
@Primary
@Profile("test")
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "false", matchIfMissing = false)
public class NoOpMqttService implements MqttService {

  public NoOpMqttService() {
    // No-op constructor for test implementation
  }

  @Override
  public boolean publishPumpStartCommand(final String deviceKey, final Long initiatedBy) {
    // No-op: return success without actually sending MQTT message
    return true;
  }

  @Override
  public boolean publishThresholdUpdate(
      final String deviceKey,
      final Double minThreshold,
      final Double maxThreshold,
      final Long updatedBy) {
    // No-op: return success without actually sending MQTT message
    return true;
  }
}
