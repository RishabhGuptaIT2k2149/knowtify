package com.knowtify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class StudyDtos {

  // ========================
  // Create Entry (preferred)
  // ========================

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor
  public static class CreateEntryRequest {
    @NotBlank(message = "Sentence is required")
    @Size(max = 1000, message = "Sentence must be less than 1000 characters")
    private String sentence;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class ParsedTopicDto {
    private String name;
    private String subject;
    private boolean isPriority;
    private String reason;
    private double confidence;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class CreateEntryResponse {
    private String message;
    private UUID entryId;
    private LocalDateTime studiedAt;
    private List<ParsedTopicDto> parsedTopics;
  }

  // ==============================
  // Aliases to match your imports
  // ==============================

  // Alias: StudyEntryRequest (same as CreateEntryRequest)
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor
  public static class StudyEntryRequest {
    @NotBlank(message = "Sentence is required")
    @Size(max = 1000, message = "Sentence must be less than 1000 characters")
    private String sentence;
  }

  // Alias: StudyEntryResponse (same as CreateEntryResponse)
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class StudyEntryResponse {
    private String message;
    private UUID entryId;
    private LocalDateTime studiedAt;
    private List<ParsedTopicDto> parsedTopics;
  }

  // =======================
  // Weekly report DTOs
  // =======================

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class WeeklyReportRequest {
    private Integer year;  // optional; default to current
    private Integer week;  // optional; default to current ISO week
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class WeeklyReportResponse {
    private ReportWeek reportWeek;
    private List<SubjectSummary> subjects;
    private List<TopicSummary> urgentReviewTopics;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class ReportWeek {
    private int year;
    private int weekNumber;
    private String startDate; // yyyy-MM-dd
    private String endDate;   // yyyy-MM-dd
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class SubjectSummary {
    private String subject;
    private List<TopicSummary> topics;
    private int totalStudies;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class TopicSummary {
    private String name;
    private int count;
    private boolean isPriority;
    private LocalDateTime lastStudiedAt;
  }
  // ---------- Knowledge Map (All-time or date range) ----------
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class DateRange {
    private String startDate; // yyyy-MM-dd (nullable for all-time)
    private String endDate;   // yyyy-MM-dd (nullable for all-time)
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class KnowledgeMapResponse {
    private DateRange dateRange; // null means “all time”
    private List<SubjectSummary> subjects;
  }
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class EntryView {
    private UUID id;
    private String originalSentence;
    private LocalDateTime studiedAt;
    private List<TopicSummary> topics; // reuse existing TopicSummary DTO
  }

}
