package com.example.waterlevel.dto;

/** DTO for authentication responses. */
public class AuthResponse {

  private String token;
  private String type = "Bearer";
  private Long expiresIn;
  private UserDto user;

  public AuthResponse() {}

  public AuthResponse(final String token, final Long expiresIn, final UserDto user) {
    this.token = token;
    this.expiresIn = expiresIn;
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public void setToken(final String token) {
    this.token = token;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(final Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public UserDto getUser() {
    return user;
  }

  public void setUser(final UserDto user) {
    this.user = user;
  }

  /** DTO for user information in auth response. */
  public static class UserDto {
    private Long id;
    private String username;
    private String email;
    private String role;

    public UserDto() {}

    public UserDto(final Long id, final String username, final String email, final String role) {
      this.id = id;
      this.username = username;
      this.email = email;
      this.role = role;
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
  }
}
