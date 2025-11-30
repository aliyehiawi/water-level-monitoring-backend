package com.example.waterlevel.constants;

/**
 * Constants for WebSocket destination paths.
 *
 * <p>Centralizes WebSocket destination definitions to avoid hardcoded strings throughout the
 * codebase.
 */
public final class WebSocketDestinations {

  private WebSocketDestinations() {
    // Utility class - prevent instantiation
  }

  /** Base path for device-specific WebSocket topics. */
  public static final String DEVICE_TOPIC_PREFIX = "/topic/device/";

  /**
   * Builds a WebSocket destination for a specific device.
   *
   * @param deviceId the device ID
   * @return the WebSocket destination path
   */
  public static String deviceTopic(final Long deviceId) {
    return DEVICE_TOPIC_PREFIX + deviceId;
  }
}
