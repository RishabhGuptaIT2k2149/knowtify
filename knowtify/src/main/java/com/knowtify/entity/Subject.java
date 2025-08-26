package com.knowtify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subjects", indexes = {
  @Index(name = "idx_subjects_name", columnList = "name", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subject {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String name; // e.g., "Data Structures & Algorithms"

  @Column(length = 500)
  private String description;

  @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Topic> topics = new ArrayList<>();
}
