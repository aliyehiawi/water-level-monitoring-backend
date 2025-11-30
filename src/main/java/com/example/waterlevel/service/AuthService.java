package com.example.waterlevel.service;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;

/**
 * Interface for authentication operations (registration and login).
 *
 * <p>Defines the contract for user authentication and registration services.
 */
public interface AuthService {

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return the authentication response with JWT token
   */
  AuthResponse register(AuthRequest request);

  /**
   * Authenticates a user and returns JWT token.
   *
   * @param request the login request
   * @return the authentication response with JWT token
   */
  AuthResponse login(AuthRequest request);
}
