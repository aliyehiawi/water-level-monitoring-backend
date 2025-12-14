package com.example.waterlevel.controller;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.service.AuthService;
import com.example.waterlevel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for authentication endpoints (register, login, get current user). */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final UserService userService;

  public AuthController(final AuthService authService, final UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return the authentication response with JWT token
   */
  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account and returns a JWT token")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User registered successfully"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data or username/email already exists")
  })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody final AuthRequest request) {
    LOGGER.info("Registration attempt for username: {}", request.getUsername());
    AuthResponse response = authService.register(request);
    LOGGER.info("User registered successfully: {}", request.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Authenticates a user and returns JWT token.
   *
   * @param request the login request
   * @return the authentication response with JWT token
   */
  @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login successful"),
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody final AuthRequest request) {
    LOGGER.info("Login attempt for username: {}", request.getUsername());
    AuthResponse response = authService.login(request);
    LOGGER.info("User logged in successfully: {}", request.getUsername());
    return ResponseEntity.ok(response);
  }

  /**
   * Gets the current authenticated user's information.
   *
   * @param authentication the authentication object
   * @return the user information
   */
  @Operation(
      summary = "Get current user",
      description = "Returns information about the currently authenticated user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(final Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      LOGGER.warn("Get current user called without authentication");
      throw new IllegalStateException("User not authenticated");
    }

    LOGGER.debug("Get current user requested for: {}", authentication.getName());
    User user = userService.getCurrentUser();

    UserResponse response =
        new UserResponse(
            user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());

    return ResponseEntity.ok(response);
  }
}
