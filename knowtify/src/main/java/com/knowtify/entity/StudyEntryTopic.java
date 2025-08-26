package com.knowtify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "study_entry_topics",
        uniqueConstraints = @UniqueConstraint(columnNames = {"study_entry_id", "topic_id"}),
        indexes = {
                @Index(name = "idx_study_entry_topics_entry", columnList = "study_entry_id"),
                @Index(name = "idx_study_entry_topics_topic", columnList = "topic_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyEntryTopic {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "study_entry_id", nullable = false)
  private StudyEntry studyEntry;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "topic_id", nullable = false)
  private Topic topic;

  @Column(name = "is_priority", nullable = false)
  @Builder.Default
  private Boolean isPriority = false;

  // Helper getter for cleaner code
  public boolean getIsPriority() {
    return isPriority != null && isPriority;
  }
}
