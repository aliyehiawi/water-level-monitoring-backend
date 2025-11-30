package com.example.waterlevel.constants;

/**
 * Constants for thread pool configuration.
 *
 * <p>Centralizes thread pool size constants to avoid magic numbers.
 */
public final class ThreadPoolConstants {

  private ThreadPoolConstants() {
    // Utility class - prevent instantiation
  }

  /** Default MQTT retry scheduler thread pool size. */
  public static final int DEFAULT_MQTT_SCHEDULER_POOL_SIZE = 2;
}
