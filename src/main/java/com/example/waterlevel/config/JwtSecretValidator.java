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

    boolean isWeakSecret = false;
    for (String weakPattern : ApplicationConstants.WEAK_SECRET_PATTERNS) {
      if (secretLower.contains(weakPattern)) {
        isWeakSecret = true;
        break;
      }
    }

    // Check for insufficient length (JWT requires minimum 32 bytes = 256 bits)
    if (jwtSecret.length() < ApplicationConstants.MIN_JWT_SECRET_LENGTH) {
      String errorMessage =
          String.format(
              "JWT secret is too short (%d characters). "
                  + "JWT HMAC-SHA algorithms require at least %d characters (256 bits). "
                  + "Please set a longer JWT_SECRET environment variable.",
              jwtSecret.length(), ApplicationConstants.MIN_JWT_SECRET_LENGTH);
      if (isProductionProfile()) {
        LOGGER.error("{} This is a security risk in production.", errorMessage);
        throw new IllegalStateException(errorMessage);
      } else {
        LOGGER.error(
            "{} The application will fail to generate JWT tokens with this secret.", errorMessage);
        throw new IllegalStateException(errorMessage);
      }
    }

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
