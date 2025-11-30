package com.example.waterlevel.entity;

/**
 * Enumeration for pump status values.
 *
 * <p>Represents the possible states of a water pump: ON, OFF, or UNKNOWN.
 */
public enum PumpStatus {
  ON,
  OFF,
  UNKNOWN;

  /**
   * Converts a string to PumpStatus enum value.
   *
   * @param status the status string
   * @return the PumpStatus enum value, or UNKNOWN if invalid
   */
  public static PumpStatus fromString(final String status) {
    if (status == null) {
      return UNKNOWN;
    }
    try {
      return valueOf(status.toUpperCase().trim());
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }
}
