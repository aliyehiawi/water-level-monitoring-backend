package com.example.waterlevel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.waterlevel.dto.AuthRequest;
import com.example.waterlevel.dto.AuthResponse;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.impl.AuthServiceImpl;
import com.example.waterlevel.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private AuditService auditService;

  @InjectMocks private AuthServiceImpl authService;

  private User testUser;
  private AuthRequest authRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("encodedPassword");
    testUser.setRole(User.Role.USER);

    authRequest = new AuthRequest();
    authRequest.setUsername("testuser");
    authRequest.setEmail("test@example.com");
    authRequest.setPassword("password123");
  }

  @Test
  void register_Success() {
    // Arrange
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtUtil.generateToken(anyString(), anyString(), any())).thenReturn("testToken");
    when(jwtUtil.getExpiration()).thenReturn(86400000L);

    // Act
    AuthResponse response = authService.register(authRequest);

    // Assert
    assertNotNull(response);
    assertEquals("testToken", response.getToken());
    assertNotNull(response.getUser());
    assertEquals("testuser", response.getUser().getUsername());
    verify(userRepository).save(any(User.class));
    verify(jwtUtil).generateToken(anyString(), anyString(), any());
  }

  @Test
  void register_UsernameExists_ThrowsException() {
    // Arrange
    when(userRepository.existsByUsername(anyString())).thenReturn(true);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> authService.register(authRequest));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void register_EmailExists_ThrowsException() {
    // Arrange
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> authService.register(authRequest));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void login_Success() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn("testuser");
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(jwtUtil.generateToken(anyString(), anyString(), any())).thenReturn("testToken");
    when(jwtUtil.getExpiration()).thenReturn(86400000L);

    // Act
    AuthResponse response = authService.login(authRequest);

    // Assert
    assertNotNull(response);
    assertEquals("testToken", response.getToken());
    assertNotNull(response.getUser());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtil).generateToken(anyString(), anyString(), any());
  }

  @Test
  void login_InvalidCredentials_ThrowsException() {
    // Arrange
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    // Act & Assert
    assertThrows(BadCredentialsException.class, () -> authService.login(authRequest));
    verify(userRepository, never()).findByUsername(anyString());
  }
}
