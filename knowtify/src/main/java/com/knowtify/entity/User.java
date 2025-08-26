package com.knowtify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
  @Index(name = "idx_users_username", columnList = "username", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 200)
  private String passwordHash;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
