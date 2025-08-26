package com.knowtify.controller;

import com.knowtify.dto.AuthDtos.*;
import com.knowtify.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    try {
      RegisterResponse response = authService.register(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
        RegisterResponse.builder()
          .message(e.getMessage())
          .build()
      );
    }
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    try {
      TokenResponse response = authService.login(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        TokenResponse.builder()
          .message(e.getMessage())
          .build()
      );
    }
  }
}
