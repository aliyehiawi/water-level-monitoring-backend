package com.example.waterlevel.service;

import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

  /**
   * Gets all users with pagination.
   *
   * @param pageable pagination information
   * @return paginated list of users
   */
  Page<UserResponse> getAllUsers(Pageable pageable);

  /**
   * Gets the current authenticated user.
   *
   * @return the current user
   * @throws IllegalArgumentException if user not found
   */
  User getCurrentUser();

  /**
   * Deletes a user by ID.
   *
   * @param userId the user ID to delete
   * @throws IllegalArgumentException if user not found or user owns devices
   */
  void deleteUser(Long userId);
}
