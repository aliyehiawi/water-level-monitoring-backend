package com.example.waterlevel.exception;

import com.example.waterlevel.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler for REST controllers. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String USER_NOT_FOUND_MESSAGE = "User not found";

  /**
   * Handles validation errors.
   *
   * @param ex the validation exception
   * @return error response
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      final MethodArgumentNotValidException ex) {
    LOGGER.warn("Validation error: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
  }

  /**
   * Handles illegal argument exceptions.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      final IllegalArgumentException ex) {
    LOGGER.warn("Illegal argument error: {}", ex.getMessage());
    String sanitizedMessage = sanitizeErrorMessage(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(sanitizedMessage));
  }

  /**
   * Handles bad credentials exceptions.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      final BadCredentialsException ex) {
    LOGGER.warn("Bad credentials error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Invalid username or password"));
  }

  /**
   * Handles username not found exceptions.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
      final UsernameNotFoundException ex) {
    LOGGER.warn("Username not found error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(USER_NOT_FOUND_MESSAGE));
  }

  /**
   * Handles JSON processing exceptions.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(JsonProcessingException.class)
  public ResponseEntity<ErrorResponse> handleJsonProcessingException(
      final JsonProcessingException ex) {
    LOGGER.warn("JSON processing error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("Invalid JSON format"));
  }

  /**
   * Handles HTTP message not readable exceptions (e.g., JSON parsing errors).
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      final HttpMessageNotReadableException ex) {
    LOGGER.warn("HTTP message not readable: {}", ex.getMessage(), ex);
    String message = ex.getMessage();
    String errorMessage =
        (message != null && message.contains("Unrecognized field"))
            ? "Invalid field in request body"
            : "Invalid request body format";
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errorMessage));
  }

  /**
   * Handles illegal state exceptions.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(final IllegalStateException ex) {
    LOGGER.warn("Illegal state error: {}", ex.getMessage(), ex);

    String message = ex.getMessage();

    // Return 401 for authentication-related IllegalStateExceptions
    if (message != null
        && (message.contains("not authenticated")
            || message.contains("User is not authenticated"))) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse("Authentication required"));
    }

    // Return 500 for other IllegalStateExceptions (e.g., system errors)
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An internal error occurred"));
  }

  /**
   * Sanitizes error messages to remove sensitive information like IDs, device keys, etc.
   *
   * @param message the original error message
   * @return sanitized error message
   */
  private String sanitizeErrorMessage(final String message) {
    if (message == null) {
      return "An error occurred";
    }

    // Remove specific IDs, device keys, and other sensitive information
    String sanitized = message;

    sanitized =
        sanitized.replaceAll(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
            "[device_key]");

    sanitized = sanitized.replaceAll("\\b\\d+\\b", "[id]");

    sanitized = sanitized.replaceAll("for key:.*", "for device");
    sanitized = sanitized.replaceAll("with id:.*", "");
    sanitized = sanitized.replaceAll("userId=.*", "userId=[id]");
    sanitized = sanitized.replaceAll("deviceId=.*", "deviceId=[id]");
    sanitized = sanitized.replaceAll("adminId=.*", "adminId=[id]");

    sanitized = sanitized.replaceAll("Device not found:.*", "Device not found");
    sanitized = sanitized.replaceAll("User not found:.*", USER_NOT_FOUND_MESSAGE);
    sanitized = sanitized.replaceAll("Admin user not found:.*", USER_NOT_FOUND_MESSAGE);
    sanitized = sanitized.replaceAll("Device not found for key:.*", "Device not found");

    return sanitized.trim();
  }

  /**
   * Handles sensor data processing exceptions.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(SensorDataProcessingException.class)
  public ResponseEntity<ErrorResponse> handleSensorDataProcessingException(
      final SensorDataProcessingException ex) {
    LOGGER.error("Sensor data processing error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Failed to process sensor data. Please try again later."));
  }

  /**
   * Handles generic exceptions as fallback.
   *
   * @param ex the exception
   * @return error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(final Exception ex) {
    LOGGER.error("Unexpected error occurred", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An unexpected error occurred"));
  }
}
