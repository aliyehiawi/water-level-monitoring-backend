package com.example.waterlevel.dto;

import com.example.waterlevel.entity.Role;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for user information responses. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String username;
  private String email;
  private Role role;
  private LocalDateTime createdAt;
}
