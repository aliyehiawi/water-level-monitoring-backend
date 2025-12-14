package com.example.waterlevel.exception;

/**
 * Exception thrown when sensor data processing fails.
 *
 * <p>This exception is used to wrap unexpected errors during sensor data processing from MQTT
 * messages, providing contextual information about the failure.
 */
public class SensorDataProcessingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new SensorDataProcessingException with the specified message.
   *
   * @param message the detail message
   */
  public SensorDataProcessingException(final String message) {
    super(message);
  }

  /**
   * Constructs a new SensorDataProcessingException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public SensorDataProcessingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
