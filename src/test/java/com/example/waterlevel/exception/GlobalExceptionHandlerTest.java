package com.example.waterlevel.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.waterlevel.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  void handleValidationExceptions_ReturnsBadRequest() {
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "field", "error message");

    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("error message", response.getBody().getErrors().get("field"));
  }

  @Test
  void handleIllegalArgumentException_ReturnsBadRequest() {
    IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getErrors().containsKey("error"));
  }

  @Test
  void handleBadCredentialsException_ReturnsUnauthorized() {
    BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(ex);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Invalid username or password", response.getBody().getErrors().get("error"));
  }

  @Test
  void handleUsernameNotFoundException_ReturnsNotFound() {
    UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleUsernameNotFoundException(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User not found", response.getBody().getErrors().get("error"));
  }

  @Test
  void handleJsonProcessingException_ReturnsBadRequest() {
    JsonProcessingException ex = mock(JsonProcessingException.class);
    when(ex.getMessage()).thenReturn("Invalid JSON");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleJsonProcessingException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Invalid JSON format", response.getBody().getErrors().get("error"));
  }

  @Test
  void handleHttpMessageNotReadableException_ReturnsBadRequest() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException(
            "Unrecognized field", new RuntimeException("Root cause"));

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleHttpMessageNotReadableException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getErrors().containsKey("error"));
  }

  @Test
  void handleGenericException_ReturnsInternalServerError() {
    Exception ex = new RuntimeException("Unexpected error");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("An unexpected error occurred", response.getBody().getErrors().get("error"));
  }
}
