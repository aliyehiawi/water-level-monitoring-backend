package com.example.waterlevel.service.impl;

import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.Role;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.DeviceRepository;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.UserService;
import com.example.waterlevel.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for user management operations.
 *
 * <p>Handles user-related business logic including role management.
 */
@Service
public class UserServiceImpl implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
  private final UserRepository userRepository;
  private final DeviceRepository deviceRepository;

  public UserServiceImpl(
      final UserRepository userRepository, final DeviceRepository deviceRepository) {
    this.userRepository = userRepository;
    this.deviceRepository = deviceRepository;
  }

  /**
   * Promotes a user to admin role.
   *
   * @param userId the user ID
   * @return the updated user
   * @throws IllegalArgumentException if user not found
   */
  @Override
  @Transactional
  public User promoteUser(final Long userId) {
    LOGGER.debug("Promoting user to admin: userId={}", userId);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setRole(Role.ADMIN);
    User updatedUser = userRepository.save(user);
    LOGGER.info(
        "User promoted to ADMIN: {} (ID: {})", updatedUser.getUsername(), updatedUser.getId());
    return updatedUser;
  }

  /**
   * Gets all users with pagination.
   *
   * @param pageable pagination information
   * @return paginated list of users
   */
  @Override
  @Transactional(readOnly = true)
  public Page<UserResponse> getAllUsers(final Pageable pageable) {
    LOGGER.debug(
        "Get all users requested: page={}, size={}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<User> users = userRepository.findAll(pageable);
    Page<UserResponse> responses =
        users.map(
            user ->
                new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getCreatedAt()));
    LOGGER.debug(
        "Returning {} users (page {})", responses.getNumberOfElements(), pageable.getPageNumber());
    return responses;
  }

  /**
   * Gets the current authenticated user.
   *
   * @return the current user
   * @throws IllegalArgumentException if user not found
   */
  @Override
  @Transactional(readOnly = true)
  public User getCurrentUser() {
    String username = SecurityUtil.getCurrentUsername();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  /**
   * Deletes a user by ID.
   *
   * @param userId the user ID to delete
   * @throws IllegalArgumentException if user not found or user owns devices
   */
  @Override
  @Transactional
  public void deleteUser(final Long userId) {
    LOGGER.debug("Deleting user: userId={}", userId);

    if (deviceRepository.existsByAdminId(userId)) {
      LOGGER.warn("User deletion failed: user owns devices - userId={}", userId);
      throw new IllegalArgumentException("Cannot delete user: user owns one or more devices");
    }

    if (!userRepository.existsById(userId)) {
      LOGGER.warn("User deletion failed: user not found - userId={}", userId);
      throw new IllegalArgumentException("User not found");
    }

    userRepository.deleteById(userId);
    LOGGER.info("User deleted successfully: userId={}", userId);
  }
}
