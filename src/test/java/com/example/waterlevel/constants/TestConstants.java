package com.example.waterlevel.constants;

/**
 * Test-specific constants.
 *
 * <p>Constants used only in test code, separate from production constants.
 */
public final class TestConstants {

  private TestConstants() {
    // Utility class - prevent instantiation
  }

  /** Test JWT secret (for unit tests only). */
  public static final String TEST_JWT_SECRET = "test-jwt-secret-for-unit-tests-only-32chars";

  /** Test JWT expiration in milliseconds (24 hours). */
  public static final Long TEST_JWT_EXPIRATION = 86400000L;
}
