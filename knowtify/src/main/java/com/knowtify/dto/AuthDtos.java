package com.knowtify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDtos {

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor
  public static class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor
  public static class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class TokenResponse {
    private String accessToken;
    private String tokenType;
    private String message;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class RegisterResponse {
    private String message;
    private String userId;
  }
}
