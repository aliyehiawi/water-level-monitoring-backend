package com.example.waterlevel.constants;

/**
 * Constants for security and CORS configuration.
 *
 * <p>Centralizes security-related constants to avoid magic numbers.
 */
public final class SecurityConstants {

  private SecurityConstants() {
    // Utility class - prevent instantiation
  }

  /** Default CORS max age in seconds (1 hour). */
  public static final long DEFAULT_CORS_MAX_AGE_SECONDS = 3600L;
}
