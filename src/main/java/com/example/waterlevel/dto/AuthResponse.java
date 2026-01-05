package com.example.waterlevel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {

  private String token;
  private String type = "Bearer";
  private Long expiresIn;
  private UserResponse user;

  public AuthResponse(final String token, final Long expiresIn, final UserResponse user) {
    this.token = token;
    this.expiresIn = expiresIn;
    this.user = user;
  }
}
