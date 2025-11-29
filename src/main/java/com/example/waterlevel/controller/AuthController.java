package com.example.waterlevel.controller;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for authentication endpoints (register, login, get current user). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;

  @Autowired
  public AuthController(final AuthService authService, final UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
  }

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return the authentication response with JWT token
   */
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody final AuthRequest request) {
    try {
      AuthResponse response = authService.register(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  /**
   * Authenticates a user and returns JWT token.
   *
   * @param request the login request
   * @return the authentication response with JWT token
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody final AuthRequest request) {
    try {
      AuthResponse response = authService.login(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  /**
   * Gets the current authenticated user's information.
   *
   * @param authentication the authentication object
   * @return the user information
   */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(final Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user =
        userRepository
            .findByUsername(authentication.getName())
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + authentication.getName()));

    UserResponse response =
        new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            user.getCreatedAt());

    return ResponseEntity.ok(response);
  }
}
