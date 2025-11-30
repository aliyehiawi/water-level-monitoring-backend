package com.example.waterlevel.constants;

/**
 * Constants for rate limiting configuration.
 *
 * <p>Centralizes rate limiting constants to avoid magic numbers.
 */
public final class RateLimitConstants {

  private RateLimitConstants() {
    // Utility class - prevent instantiation
  }

  /** Default requests per minute per IP. */
  public static final int DEFAULT_REQUESTS_PER_MINUTE = 100;

  /** Bucket expiration time in minutes (cleanup inactive buckets). */
  public static final int BUCKET_EXPIRATION_MINUTES = 5;

  /** Default maximum cache size for rate limiting buckets. */
  public static final int DEFAULT_CACHE_MAX_SIZE = 10000;
}
