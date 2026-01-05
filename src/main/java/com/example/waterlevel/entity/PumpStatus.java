package com.example.waterlevel.entity;

public enum PumpStatus {
  ON,
  OFF,
  UNKNOWN;

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
