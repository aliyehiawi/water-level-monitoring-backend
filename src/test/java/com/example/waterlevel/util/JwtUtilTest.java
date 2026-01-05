package com.example.waterlevel.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

  @Autowired private JwtUtil jwtUtil;

  @Value("${spring.security.jwt.expiration}")
  private Long testJwtExpiration;

  @Test
  void generateToken_Success() {
    String token = jwtUtil.generateToken("testuser", "USER", 1L);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void getUsernameFromToken_Success() {
    String token = jwtUtil.generateToken("testuser", "USER", 1L);

    String username = jwtUtil.getUsernameFromToken(token);

    assertEquals("testuser", username);
  }

  @Test
  void getRoleFromToken_Success() {
    String token = jwtUtil.generateToken("testuser", "ADMIN", 1L);

    String role = jwtUtil.getRoleFromToken(token);

    assertEquals("ADMIN", role);
  }

  @Test
  void getUserIdFromToken_Success() {
    String token = jwtUtil.generateToken("testuser", "USER", 123L);

    Long userId = jwtUtil.getUserIdFromToken(token);

    assertEquals(123L, userId);
  }

  @Test
  void validateToken_ValidToken_ReturnsTrue() {
    String token = jwtUtil.generateToken("testuser", "USER", 1L);

    boolean isValid = jwtUtil.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  void validateToken_InvalidToken_ReturnsFalse() {
    boolean isValid = jwtUtil.validateToken("invalid.token.here");

    assertFalse(isValid);
  }

  @Test
  void getExpiration_ReturnsCorrectValue() {
    Long expiration = jwtUtil.getExpiration();

    assertEquals(testJwtExpiration, expiration);
  }
}
