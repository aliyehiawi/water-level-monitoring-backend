package com.example.waterlevel.constants;

/**
 * Constants for WebSocket message types.
 *
 * <p>Centralizes message type definitions to avoid hardcoded strings throughout the codebase.
 */
public final class MessageTypes {

  private MessageTypes() {
    // Utility class - prevent instantiation
  }

  /** Message type for sensor data updates. */
  public static final String SENSOR_UPDATE = "sensor_update";

  /** Message type for pump status updates. */
  public static final String PUMP_STATUS = "pump_status";

  /** Message type for threshold update confirmations. */
  public static final String THRESHOLD_UPDATED = "threshold_updated";
}
