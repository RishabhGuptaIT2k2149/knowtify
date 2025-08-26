package com.knowtify.service;

import com.knowtify.dto.StudyDtos.*;
import com.knowtify.entity.StudyEntry;
import com.knowtify.entity.StudyEntryTopic;
import com.knowtify.repository.StudyEntryRepository;
import com.knowtify.util.WeekUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private final StudyEntryRepository studyEntryRepository;
  private final WeekUtil weekUtil;

  @Transactional(readOnly = true)
  public WeeklyReportResponse generateWeeklyReport(UUID userId, Integer year, Integer week) {
    // Use current week if not specified
    if (year == null) year = weekUtil.getCurrentYear();
    if (week == null) week = weekUtil.getCurrentWeekNumber();

    WeekUtil.WeekRange weekRange = weekUtil.getWeekRange(year, week);

    // Get entries for the week
    LocalDateTime weekStart = weekRange.getStartDate().atStartOfDay();
    LocalDateTime weekEnd = weekRange.getEndDate().atTime(LocalTime.MAX);

    List<StudyEntry> entries = studyEntryRepository
            .findByUser_IdAndStudiedAtBetween(userId, weekStart, weekEnd);

    // Collect all study entry topics
    List<StudyEntryTopic> allTopics = entries.stream()
            .flatMap(entry -> entry.getStudyEntryTopics().stream())
            .collect(Collectors.toList());

    // Group by subject
    Map<String, List<StudyEntryTopic>> topicsBySubject = allTopics.stream()
            .collect(Collectors.groupingBy(
                    studyEntryTopic -> studyEntryTopic.getTopic().getSubject().getName()
            ));

    List<SubjectSummary> subjects = new ArrayList<>();
    List<TopicSummary> urgentTopics = new ArrayList<>();

    for (Map.Entry<String, List<StudyEntryTopic>> subjectEntry : topicsBySubject.entrySet()) {
      String subjectName = subjectEntry.getKey();
      List<StudyEntryTopic> subjectTopics = subjectEntry.getValue();

      // Group by topic name
      Map<String, List<StudyEntryTopic>> topicGroups = subjectTopics.stream()
              .collect(Collectors.groupingBy(
                      studyEntryTopic -> studyEntryTopic.getTopic().getName()
              ));

      List<TopicSummary> topicSummaries = new ArrayList<>();

      for (Map.Entry<String, List<StudyEntryTopic>> topicGroup : topicGroups.entrySet()) {
        String topicName = topicGroup.getKey();
        List<StudyEntryTopic> topicInstances = topicGroup.getValue();

        boolean hasAnyPriority = topicInstances.stream()
                .anyMatch(StudyEntryTopic::getIsPriority);

        LocalDateTime lastStudied = topicInstances.stream()
                .map(studyEntryTopic -> studyEntryTopic.getStudyEntry().getStudiedAt())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        TopicSummary topicSummary = TopicSummary.builder()
                .name(topicName)
                .count(topicInstances.size())
                .isPriority(hasAnyPriority)
                .lastStudiedAt(lastStudied)
                .build();

        topicSummaries.add(topicSummary);

        if (hasAnyPriority) {
          urgentTopics.add(topicSummary);
        }
      }

      // Sort topics by count descending, then by name
      topicSummaries.sort((a, b) -> {
        int countCompare = Integer.compare(b.getCount(), a.getCount());
        return countCompare != 0 ? countCompare : a.getName().compareTo(b.getName());
      });

      subjects.add(SubjectSummary.builder()
              .subject(subjectName)
              .topics(topicSummaries)
              .totalStudies(subjectTopics.size())
              .build());
    }

    // Sort subjects by total studies descending
    subjects.sort((a, b) -> Integer.compare(b.getTotalStudies(), a.getTotalStudies()));

    // Sort urgent topics by last studied descending
    urgentTopics.sort((a, b) -> {
      if (a.getLastStudiedAt() == null && b.getLastStudiedAt() == null) return 0;
      if (a.getLastStudiedAt() == null) return 1;
      if (b.getLastStudiedAt() == null) return -1;
      return b.getLastStudiedAt().compareTo(a.getLastStudiedAt());
    });

    return WeeklyReportResponse.builder()
            .reportWeek(ReportWeek.builder()
                    .year(year)
                    .weekNumber(week)
                    .startDate(weekRange.getStartDate().toString())
                    .endDate(weekRange.getEndDate().toString())
                    .build())
            .subjects(subjects)
            .urgentReviewTopics(urgentTopics)
            .build();
  }
}
