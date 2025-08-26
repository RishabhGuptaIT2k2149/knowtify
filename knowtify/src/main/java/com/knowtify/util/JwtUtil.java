package com.knowtify.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

  private final SecretKey secretKey;
  private final long expirationHours;

  public JwtUtil(
    @Value("${jwt.secret}") String secret,
    @Value("${jwt.expiration-hours:1}") long expirationHours
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationHours = expirationHours;
  }

  public String generateToken(UUID userId, String username) {
    Instant now = Instant.now();
    // Using plusSeconds instead of ChronoUnit to avoid import issues
    Instant expiration = now.plusSeconds(expirationHours * 3600);

    return Jwts.builder()
      .setSubject(username)
      .claim("user_id", userId.toString())
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(expiration))
      .signWith(secretKey, SignatureAlgorithm.HS256)
      .compact();
  }

  public Claims validateToken(String token) {
    try {
      return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    } catch (ExpiredJwtException e) {
      log.warn("JWT token has expired");
      throw new RuntimeException("Token expired");
    } catch (JwtException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      throw new RuntimeException("Invalid token");
    }
  }

  public UUID getUserIdFromToken(String token) {
    Claims claims = validateToken(token);
    String userIdStr = claims.get("user_id", String.class);
    return UUID.fromString(userIdStr);
  }

  public String getUsernameFromToken(String token) {
    Claims claims = validateToken(token);
    return claims.getSubject();
  }

  public boolean isTokenExpired(String token) {
    try {
      Claims claims = validateToken(token);
      return claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
}
