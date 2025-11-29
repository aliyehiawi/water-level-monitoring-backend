package com.example.waterlevel.controller;

import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for user management endpoints (admin only). */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  private final UserRepository userRepository;

  @Autowired
  public UserController(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Gets all users.
   *
   * @return list of all users
   */
  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<User> users = userRepository.findAll();
    List<UserResponse> responses =
        users.stream()
            .map(
                user ->
                    new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCreatedAt()))
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Promotes a user to admin role.
   *
   * @param id the user ID
   * @return the updated user
   */
  @PutMapping("/{id}/promote")
  public ResponseEntity<UserResponse> promoteUser(@PathVariable final Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

    user.setRole(User.Role.ADMIN);
    User updatedUser = userRepository.save(user);

    UserResponse response =
        new UserResponse(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getRole().name(),
            updatedUser.getCreatedAt());

    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a user.
   *
   * @param id the user ID
   * @return no content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable final Long id) {
    if (!userRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    userRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
