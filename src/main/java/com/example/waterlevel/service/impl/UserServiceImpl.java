package com.example.waterlevel.service.impl;

import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public UserServiceImpl(final UserRepository userRepository) {
    this.userRepository = userRepository;
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

    user.setRole(User.Role.ADMIN);
    User updatedUser = userRepository.save(user);
    LOGGER.info(
        "User promoted to ADMIN: {} (ID: {})", updatedUser.getUsername(), updatedUser.getId());
    return updatedUser;
  }
}
