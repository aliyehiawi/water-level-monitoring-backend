package com.example.waterlevel.service;

import com.example.waterlevel.entity.User;

/**
 * Interface for user management operations.
 *
 * <p>Defines the contract for user-related business logic including role management.
 */
public interface UserService {

  /**
   * Promotes a user to admin role.
   *
   * @param userId the user ID
   * @return the updated user
   * @throws IllegalArgumentException if user not found
   */
  User promoteUser(Long userId);
}
