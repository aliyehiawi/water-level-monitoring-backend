package com.example.waterlevel.service;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for authentication operations (registration and login). */
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  @Autowired
  public AuthService(
      final UserRepository userRepository,
      final PasswordEncoder passwordEncoder,
      final JwtUtil jwtUtil,
      final AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
  }

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return the authentication response with JWT token
   */
  @Transactional
  public AuthResponse register(final AuthRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    // Create new user
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(User.Role.USER);

    User savedUser = userRepository.save(user);

    // Generate JWT token
    String token =
        jwtUtil.generateToken(
            savedUser.getUsername(), savedUser.getRole().name(), savedUser.getId());

    // Build response
    UserResponse userResponse =
        new UserResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole().name(),
            savedUser.getCreatedAt());

    return new AuthResponse(token, jwtUtil.getExpiration() / 1000, userResponse);
  }

  /**
   * Authenticates a user and returns JWT token.
   *
   * @param request the login request
   * @return the authentication response with JWT token
   */
  public AuthResponse login(final AuthRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    User user =
        userRepository
            .findByUsername(authentication.getName())
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + authentication.getName()));

    // Generate JWT token
    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());

    // Build response
    UserResponse userResponse =
        new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            user.getCreatedAt());

    return new AuthResponse(token, jwtUtil.getExpiration() / 1000, userResponse);
  }
}
