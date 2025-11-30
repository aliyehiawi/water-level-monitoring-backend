package com.example.waterlevel.util;

import static org.junit.jupiter.api.Assertions.*;

import com.example.waterlevel.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", TestConstants.TEST_JWT_SECRET);
    ReflectionTestUtils.setField(jwtUtil, "expiration", TestConstants.TEST_JWT_EXPIRATION);
  }

  @Test
  void generateToken_Success() {
    // Act
    String token = jwtUtil.generateToken("testuser", "USER", 1L);

    // Assert
    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void getUsernameFromToken_Success() {
    // Arrange
    String token = jwtUtil.generateToken("testuser", "USER", 1L);

    // Act
    String username = jwtUtil.getUsernameFromToken(token);

    // Assert
    assertEquals("testuser", username);
  }

  @Test
  void getRoleFromToken_Success() {
    // Arrange
    String token = jwtUtil.generateToken("testuser", "ADMIN", 1L);

    // Act
    String role = jwtUtil.getRoleFromToken(token);

    // Assert
    assertEquals("ADMIN", role);
  }

  @Test
  void getUserIdFromToken_Success() {
    // Arrange
    String token = jwtUtil.generateToken("testuser", "USER", 123L);

    // Act
    Long userId = jwtUtil.getUserIdFromToken(token);

    // Assert
    assertEquals(123L, userId);
  }

  @Test
  void validateToken_ValidToken_ReturnsTrue() {
    // Arrange
    String token = jwtUtil.generateToken("testuser", "USER", 1L);

    // Act
    boolean isValid = jwtUtil.validateToken(token);

    // Assert
    assertTrue(isValid);
  }

  @Test
  void validateToken_InvalidToken_ReturnsFalse() {
    // Act
    boolean isValid = jwtUtil.validateToken("invalid.token.here");

    // Assert
    assertFalse(isValid);
  }

  @Test
  void getExpiration_ReturnsCorrectValue() {
    // Act
    Long expiration = jwtUtil.getExpiration();

    // Assert
    assertEquals(TestConstants.TEST_JWT_EXPIRATION, expiration);
  }
}
