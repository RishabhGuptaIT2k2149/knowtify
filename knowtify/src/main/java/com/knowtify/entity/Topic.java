package com.knowtify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "topics", indexes = {
        @Index(name = "idx_topics_name", columnList = "name"),
        @Index(name = "idx_topics_subject", columnList = "subject_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Topic {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, length = 128)
  private String name; // e.g., "sorting algorithms"

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "subject_id", nullable = false)
  private Subject subject;

  @Column(name = "confidence_score")
  private Double confidenceScore; // Gemini's confidence in categorization
}
