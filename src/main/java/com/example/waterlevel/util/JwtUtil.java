package com.example.waterlevel.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Utility class for JWT token generation and validation. */
@Component
public class JwtUtil {

  @Value("${spring.security.jwt.secret}")
  private String secret;

  @Value("${spring.security.jwt.expiration}")
  private Long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generates a JWT token for the given username and role.
   *
   * @param username the username
   * @param role the user role
   * @return the JWT token
   */
  public String generateToken(final String username, final String role, final Long userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .claim("userId", userId)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Extracts the username from a JWT token.
   *
   * @param token the JWT token
   * @return the username
   */
  public String getUsernameFromToken(final String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.getSubject();
  }

  /**
   * Extracts the role from a JWT token.
   *
   * @param token the JWT token
   * @return the role
   */
  public String getRoleFromToken(final String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("role", String.class);
  }

  /**
   * Extracts the user ID from a JWT token.
   *
   * @param token the JWT token
   * @return the user ID
   */
  public Long getUserIdFromToken(final String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("userId", Long.class);
  }

  /**
   * Validates a JWT token.
   *
   * @param token the JWT token
   * @return true if valid, false otherwise
   */
  public boolean validateToken(final String token) {
    try {
      Claims claims = getClaimsFromToken(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private Claims getClaimsFromToken(final String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Gets the expiration time in milliseconds.
   *
   * @return expiration time
   */
  public Long getExpiration() {
    return expiration;
  }
}
