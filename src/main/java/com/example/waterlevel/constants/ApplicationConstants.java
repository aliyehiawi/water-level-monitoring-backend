package com.example.waterlevel.constants;

/**
 * Application-wide constants.
 *
 * <p>Centralizes application constants to avoid magic numbers and hardcoded values.
 */
public final class ApplicationConstants {

  private ApplicationConstants() {
    // Utility class - prevent instantiation
  }

  /** Minimum JWT secret length for security. */
  public static final int MIN_JWT_SECRET_LENGTH = 32;

  /**
   * Common weak JWT secret patterns to detect (for validation only).
   *
   * <p>Must be public as it's used across packages (e.g., JwtSecretValidator).
   */
  @SuppressWarnings("java:S2386") // Array is intentionally mutable for validation purposes
  public static final String[] WEAK_SECRET_PATTERNS = {
    "secret", "password", "123456", "changeme", "default"
  };

  /** Minimum water level value. */
  public static final double MIN_WATER_LEVEL = 0.0;

  /** Maximum water level value. */
  public static final double MAX_WATER_LEVEL = 999.99;

  /** UUID length (standard UUID format). */
  public static final int UUID_LENGTH = 36;

  /** Default MQTT retry max attempts. */
  public static final int DEFAULT_MQTT_RETRY_MAX_ATTEMPTS = 3;

  /** Default MQTT retry initial delay in milliseconds. */
  public static final long DEFAULT_MQTT_RETRY_INITIAL_DELAY_MS = 1000L;

  /** Default MQTT retry max delay in milliseconds. */
  public static final long DEFAULT_MQTT_RETRY_MAX_DELAY_MS = 10000L;

  /** Default MQTT retry multiplier. */
  public static final double DEFAULT_MQTT_RETRY_MULTIPLIER = 2.0;

  /** MQTT scheduler shutdown timeout in seconds. */
  public static final int MQTT_SCHEDULER_SHUTDOWN_TIMEOUT_SECONDS = 5;

  /** Error message for device not found. */
  public static final String DEVICE_NOT_FOUND_MESSAGE = "Device not found";
}
