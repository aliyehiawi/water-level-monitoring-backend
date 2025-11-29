package com.example.waterlevel.dto;

import java.time.LocalDateTime;

/** DTO for user information responses. */
public class UserResponse {

  private Long id;
  private String username;
  private String email;
  private String role;
  private LocalDateTime createdAt;

  public UserResponse() {}

  public UserResponse(
      final Long id,
      final String username,
      final String email,
      final String role,
      final LocalDateTime createdAt) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.role = role;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
