package com.example.waterlevel.config;

import com.example.waterlevel.constants.ApplicationConstants;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** Validates JWT secret configuration to prevent using default secret in production. */
@Configuration
public class JwtSecretValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtSecretValidator.class);

  @Value("${spring.security.jwt.secret}")
  private String jwtSecret;

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  @PostConstruct
  public void validateJwtSecret() {
    if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
      LOGGER.error("JWT_SECRET is not configured. Please set JWT_SECRET environment variable.");
      throw new IllegalStateException(
          "JWT_SECRET is required. Please set the JWT_SECRET environment variable.");
    }

    String secretLower = jwtSecret.toLowerCase().trim();

    // Check for weak secret patterns
    boolean isWeakSecret = false;
    for (String weakPattern : ApplicationConstants.WEAK_SECRET_PATTERNS) {
      if (secretLower.contains(weakPattern)) {
        isWeakSecret = true;
        break;
      }
    }

    // Check for insufficient length
    if (jwtSecret.length() < ApplicationConstants.MIN_JWT_SECRET_LENGTH) {
      if (isProductionProfile()) {
        LOGGER.error(
            "JWT secret is too short ({} characters). Minimum recommended length is {} characters. "
                + "This is a security risk in production.",
            jwtSecret.length(),
            ApplicationConstants.MIN_JWT_SECRET_LENGTH);
        throw new IllegalStateException(
            "JWT secret is too short for production. "
                + "Please set a strong JWT_SECRET environment variable with at least "
                + ApplicationConstants.MIN_JWT_SECRET_LENGTH
                + " characters.");
      } else {
        LOGGER.warn(
            "JWT secret is shorter than recommended minimum length of {} characters. "
                + "Consider using a longer secret for better security.",
            ApplicationConstants.MIN_JWT_SECRET_LENGTH);
      }
    }

    // Check for weak patterns in production
    if (isWeakSecret && isProductionProfile()) {
      LOGGER.error(
          "Weak JWT secret pattern detected in production environment. "
              + "This is a security risk. Please set a strong, random JWT_SECRET environment variable.");
      throw new IllegalStateException(
          "Weak JWT secret cannot be used in production. "
              + "Please set a strong, random JWT_SECRET environment variable.");
    } else if (isWeakSecret) {
      LOGGER.warn(
          "Weak JWT secret pattern detected. This is acceptable for development only. "
              + "For production, set a strong, random JWT_SECRET environment variable.");
    }
  }

  private boolean isProductionProfile() {
    return activeProfile != null
        && (activeProfile.contains("prod") || activeProfile.contains("production"));
  }
}
