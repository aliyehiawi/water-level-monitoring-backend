package com.example.waterlevel.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationFlowIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void completeAuthenticationFlow() throws Exception {
    // Step 1: Register a new user
    AuthRequest registerRequest = new AuthRequest();
    registerRequest.setUsername("testuser");
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password123");

    String registerResponse =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.username").value("testuser"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    AuthResponse registerAuthResponse =
        objectMapper.readValue(registerResponse, AuthResponse.class);
    String token = registerAuthResponse.getToken();
    assertNotNull(token);

    // Step 2: Login with registered credentials
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    String loginResponse =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    AuthResponse loginAuthResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
    assertNotNull(loginAuthResponse.getToken());

    // Step 3: Get current user with token
    mockMvc
        .perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));

    // Verify user was created in database
    User savedUser = userRepository.findByUsername("testuser").orElse(null);
    assertNotNull(savedUser);
    assertEquals("test@example.com", savedUser.getEmail());
    assertEquals(User.Role.USER, savedUser.getRole());
  }

  @Test
  void register_DuplicateUsername_ReturnsBadRequest() throws Exception {
    // Create first user
    User existingUser = new User();
    existingUser.setUsername("existinguser");
    existingUser.setEmail("existing@example.com");
    existingUser.setPassword(passwordEncoder.encode("password123"));
    existingUser.setRole(User.Role.USER);
    userRepository.save(existingUser);

    // Try to register with same username
    AuthRequest request = new AuthRequest();
    request.setUsername("existinguser");
    request.setEmail("new@example.com");
    request.setPassword("password123");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
