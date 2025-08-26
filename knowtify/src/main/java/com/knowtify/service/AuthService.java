package com.knowtify.service;

import com.knowtify.dto.AuthDtos.*;
import com.knowtify.entity.User;
import com.knowtify.repository.UserRepository;
import com.knowtify.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public RegisterResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Username already exists");
    }

    // Create new user
    User user = User.builder()
      .username(request.getUsername())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .build();

    user = userRepository.save(user);

    return RegisterResponse.builder()
      .message("User registered successfully")
      .userId(user.getId().toString())
      .build();
  }

  public TokenResponse login(LoginRequest request) {
    // Find user by username
    User user = userRepository.findByUsername(request.getUsername())
      .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    // Check password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new RuntimeException("Invalid credentials");
    }

    // Generate JWT token
    String token = jwtUtil.generateToken(user.getId(), user.getUsername());

    return TokenResponse.builder()
      .accessToken(token)
      .tokenType("bearer")
      .message("Login successful")
      .build();
  }
}
