package com.example.waterlevel.constants;

/**
 * Constants for MQTT topic patterns and destinations.
 *
 * <p>Centralizes MQTT topic definitions to avoid hardcoded strings throughout the codebase.
 */
public final class MqttTopics {

  private MqttTopics() {
    // Utility class - prevent instantiation
  }

  /** Base path for device-specific topics. */
  public static final String DEVICES_BASE = "devices/";

  /** Topic pattern for sensor data from devices (wildcard: + matches device key). */
  public static final String SENSOR_DATA_PATTERN = DEVICES_BASE + "+/sensor/data";

  /** Topic pattern for pump start commands (device key replaces {deviceKey}). */
  public static final String PUMP_START_PATTERN = DEVICES_BASE + "{deviceKey}/pump/start";

  /** Topic pattern for threshold updates (device key replaces {deviceKey}). */
  public static final String THRESHOLD_UPDATE_PATTERN =
      DEVICES_BASE + "{deviceKey}/thresholds/update";

  /**
   * Builds a pump start topic for a specific device.
   *
   * @param deviceKey the device key (UUID)
   * @return the MQTT topic for pump start command
   */
  public static String pumpStartTopic(final String deviceKey) {
    return DEVICES_BASE + deviceKey + "/pump/start";
  }

  /**
   * Builds a threshold update topic for a specific device.
   *
   * @param deviceKey the device key (UUID)
   * @return the MQTT topic for threshold update
   */
  public static String thresholdUpdateTopic(final String deviceKey) {
    return DEVICES_BASE + deviceKey + "/thresholds/update";
  }
}
