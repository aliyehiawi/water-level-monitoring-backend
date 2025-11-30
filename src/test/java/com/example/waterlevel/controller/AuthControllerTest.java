package com.example.waterlevel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.dto.UserResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthService authService;
  @MockBean private UserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void register_Success() throws Exception {
    // Arrange
    AuthRequest request = new AuthRequest();
    request.setUsername("testuser");
    request.setEmail("test@example.com");
    request.setPassword("password123");

    UserResponse userResponse =
        new UserResponse(1L, "testuser", "test@example.com", User.Role.USER, LocalDateTime.now());
    AuthResponse authResponse = new AuthResponse("testToken", 86400L, userResponse);

    when(authService.register(any(AuthRequest.class))).thenReturn(authResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("testToken"))
        .andExpect(jsonPath("$.user.username").value("testuser"));
  }

  @Test
  void register_InvalidRequest_ReturnsBadRequest() throws Exception {
    // Arrange
    AuthRequest request = new AuthRequest();
    request.setUsername(""); // Invalid

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_Success() throws Exception {
    // Arrange
    AuthRequest request = new AuthRequest();
    request.setUsername("testuser");
    request.setPassword("password123");

    UserResponse userResponse =
        new UserResponse(1L, "testuser", "test@example.com", User.Role.USER, LocalDateTime.now());
    AuthResponse authResponse = new AuthResponse("testToken", 86400L, userResponse);

    when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("testToken"))
        .andExpect(jsonPath("$.user.username").value("testuser"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void getCurrentUser_Success() throws Exception {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(User.Role.USER);
    user.setCreatedAt(LocalDateTime.now());

    when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));

    // Act & Assert
    mockMvc
        .perform(get("/api/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  void getCurrentUser_Unauthenticated_ReturnsUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }
}
