package com.example.waterlevel.constants;

/**
 * Constants for MQTT configuration values.
 *
 * <p>Centralizes MQTT-related constants to avoid magic numbers.
 */
public final class MqttConstants {

  private MqttConstants() {
    // Utility class - prevent instantiation
  }

  /** Default MQTT connection timeout in seconds. */
  public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;

  /** Default MQTT keep-alive interval in seconds. */
  public static final int DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS = 60;

  /** Default MQTT QoS level. */
  public static final int DEFAULT_QOS_LEVEL = 1;

  /** Pump start command string. */
  public static final String PUMP_START_COMMAND = "START";
}
