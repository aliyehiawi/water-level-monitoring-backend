package com.example.waterlevel.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Utility class for security-related operations. */
public class SecurityUtil {

  private SecurityUtil() {
    // Utility class - prevent instantiation
  }

  /**
   * Gets the current authenticated user's username.
   *
   * @return the username
   * @throws IllegalStateException if user is not authenticated
   */
  public static String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new IllegalStateException("User is not authenticated");
    }
    return authentication.getName();
  }
}
