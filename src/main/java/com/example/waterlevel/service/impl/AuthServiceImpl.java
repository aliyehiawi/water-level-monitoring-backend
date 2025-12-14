package com.example.waterlevel.service.impl;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.Role;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.AuthService;
import com.example.waterlevel.util.JwtUtil;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for authentication operations (registration and login). */
@Service
public class AuthServiceImpl implements AuthService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;
  private final AuditService auditService;

  public AuthServiceImpl(
      final UserRepository userRepository,
      final PasswordEncoder passwordEncoder,
      final JwtUtil jwtUtil,
      final AuthenticationManager authenticationManager,
      final AuditService auditService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
    this.auditService = auditService;
  }

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return the authentication response with JWT token
   */
  @Override
  @Transactional
  public AuthResponse register(final AuthRequest request) {
    LOGGER.debug("Registering new user: {}", request.getUsername());

    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
      LOGGER.warn("Registration failed: email is required");
      throw new IllegalArgumentException("Email is required");
    }

    if (userRepository.existsByUsername(request.getUsername())) {
      LOGGER.warn("Registration failed: username already exists - {}", request.getUsername());
      throw new IllegalArgumentException("Username already exists");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      LOGGER.warn("Registration failed: email already exists - {}", request.getEmail());
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.USER);

    User savedUser = userRepository.save(user);
    LOGGER.info(
        "User registered successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
    auditService.logUserRegistration(savedUser.getId(), savedUser.getUsername());

    String token =
        jwtUtil.generateToken(
            savedUser.getUsername(), savedUser.getRole().name(), savedUser.getId());

    UserResponse userResponse =
        new UserResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getCreatedAt());

    long expirationSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtUtil.getExpiration());
    return new AuthResponse(token, expirationSeconds, userResponse);
  }

  /**
   * Authenticates a user and returns JWT token.
   *
   * @param request the login request
   * @return the authentication response with JWT token
   */
  @Override
  @Transactional(readOnly = true)
  public AuthResponse login(final AuthRequest request) {
    LOGGER.debug("Attempting login for user: {}", request.getUsername());
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    User user =
        userRepository
            .findByUsername(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    LOGGER.info("User logged in successfully: {} (ID: {})", user.getUsername(), user.getId());

    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());

    UserResponse userResponse =
        new UserResponse(
            user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());

    long expirationSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtUtil.getExpiration());
    return new AuthResponse(token, expirationSeconds, userResponse);
  }
}
