package com.example.waterlevel.controller;

import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.service.AuditService;
import com.example.waterlevel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for user management endpoints (admin only). */
@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "Admin operations for managing users")
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  private final AuditService auditService;
  private final UserService userService;

  public UserController(final AuditService auditService, final UserService userService) {
    this.auditService = auditService;
    this.userService = userService;
  }

  /**
   * Gets all users with pagination.
   *
   * @param page page number (default: 0)
   * @param size page size (default: 20)
   * @return paginated list of users
   */
  @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users")
  @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
  @GetMapping
  public ResponseEntity<Page<UserResponse>> getAllUsers(
      @Parameter(description = "Page number (0-indexed)", example = "0")
          @RequestParam(defaultValue = "0")
          @Min(value = 0, message = "Page number must be >= 0")
          final int page,
      @Parameter(description = "Page size", example = "20")
          @RequestParam(defaultValue = "20")
          @Min(value = 1, message = "Page size must be >= 1")
          @Max(value = 100, message = "Page size must be <= 100")
          final int size) {
    LOGGER.debug("Get all users requested: page={}, size={}", page, size);
    Pageable pageable = PageRequest.of(page, size);
    Page<UserResponse> responses = userService.getAllUsers(pageable);
    LOGGER.debug("Returning {} users (page {})", responses.getNumberOfElements(), page);
    return ResponseEntity.ok(responses);
  }

  /**
   * Promotes a user to admin role.
   *
   * @param id the user ID
   * @return the updated user
   */
  @Operation(
      summary = "Promote user to admin",
      description = "Promotes a regular user to admin role")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User promoted successfully"),
    @ApiResponse(responseCode = "400", description = "User not found")
  })
  @PutMapping("/{id}/promote")
  public ResponseEntity<UserResponse> promoteUser(
      @Parameter(description = "User ID", example = "2") @PathVariable final Long id) {
    LOGGER.info("User promotion request for userId: {}", id);
    User admin = userService.getCurrentUser();

    User updatedUser = userService.promoteUser(id);
    auditService.logUserPromotion(admin.getId(), updatedUser.getId(), updatedUser.getUsername());

    UserResponse response =
        new UserResponse(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getRole(),
            updatedUser.getCreatedAt());

    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a user.
   *
   * @param id the user ID
   * @return no content
   */
  @Operation(summary = "Delete user", description = "Deletes a user by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "User ID", example = "3") @PathVariable final Long id) {
    LOGGER.info("User deletion request for userId: {}", id);
    User admin = userService.getCurrentUser();

    userService.deleteUser(id);
    auditService.logUserDeletion(admin.getId(), id);
    return ResponseEntity.noContent().build();
  }
}
