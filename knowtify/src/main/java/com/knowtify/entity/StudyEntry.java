package com.knowtify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "study_entries", indexes = {
        @Index(name = "idx_study_entries_user", columnList = "user_id"),
        @Index(name = "idx_study_entries_studied_at", columnList = "studied_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyEntry {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "original_sentence", nullable = false, length = 1000)
  private String originalSentence;

  // Existing field used in code
  @Column(name = "studied_at", nullable = false)
  private LocalDateTime studiedAt;

  // Add this to satisfy existing DB column
  @Column(name = "recorded_at", nullable = false)
  private LocalDateTime recordedAt;

  @OneToMany(mappedBy = "studyEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<StudyEntryTopic> studyEntryTopics = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (studiedAt == null) {
      studiedAt = now;
    }
    if (recordedAt == null) {
      recordedAt = now;
    }
  }
}
