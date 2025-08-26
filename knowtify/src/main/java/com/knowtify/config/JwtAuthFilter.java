package com.knowtify.config;
import com.knowtify.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
      throws ServletException, IOException {
    
    String token = extractTokenFromHeader(request);
    
    if (token != null) {
      try {
        UUID userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        
        // Create authentication object with user info
        var authentication = new UsernamePasswordAuthenticationToken(
          new AuthenticatedUser(userId, username),
          null,
          Collections.emptyList() // No roles for MVP
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
      } catch (Exception e) {
        log.warn("JWT validation failed: {}", e.getMessage());
        // Continue without authentication
      }
    }
    
    filterChain.doFilter(request, response);
  }

  private String extractTokenFromHeader(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
      return authHeader.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  public static record AuthenticatedUser(UUID userId, String username) {}
}
