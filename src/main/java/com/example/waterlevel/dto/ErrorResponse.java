package com.example.waterlevel.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Standardized error response DTO.
 *
 * <p>Provides consistent error response format across all API endpoints.
 */
@Getter
public class ErrorResponse {
  private final Map<String, String> errors;

  /**
   * Creates an error response with a single error message.
   *
   * @param error the error message
   */
  public ErrorResponse(final String error) {
    this.errors = new HashMap<>();
    this.errors.put("error", error);
  }

  /**
   * Creates an error response with multiple field errors.
   *
   * @param errors map of field names to error messages
   */
  public ErrorResponse(final Map<String, String> errors) {
    this.errors = errors != null ? new HashMap<>(errors) : new HashMap<>();
  }

  /**
   * Creates an error response from a single field error.
   *
   * @param field the field name
   * @param message the error message
   */
  public ErrorResponse(final String field, final String message) {
    this.errors = new HashMap<>();
    this.errors.put(field, message);
  }
}
