package com.knowtify.service;

import com.knowtify.dto.StudyDtos.DateRange;
import com.knowtify.dto.StudyDtos.KnowledgeMapResponse;
import com.knowtify.dto.StudyDtos.SubjectSummary;
import com.knowtify.dto.StudyDtos.TopicSummary;
import com.knowtify.entity.StudyEntry;
import com.knowtify.entity.StudyEntryTopic;
import com.knowtify.repository.StudyEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeMapService {

  private final StudyEntryRepository studyEntryRepository;

  @Transactional
  public KnowledgeMapResponse getKnowledgeMap(UUID userId, LocalDate start, LocalDate end) {
    List<StudyEntry> entries;
    if (start != null && end != null) {
      LocalDateTime from = start.atStartOfDay();
      LocalDateTime to = end.plusDays(1).atStartOfDay().minusNanos(1); // inclusive end
      entries = studyEntryRepository.findByUser_IdAndStudiedAtBetween(userId, from, to);
    } else {
      // If you don't have a dedicated "findAllByUserId", filter in memory
      entries = studyEntryRepository.findAll().stream()
          .filter(e -> e.getUser() != null && userId.equals(e.getUser().getId()))
          .collect(Collectors.toList());
    }

    // subject -> topic -> accumulator
    Map<String, Map<String, TopicAccumulator>> subjectTopicMap = new LinkedHashMap<>();

    for (StudyEntry entry : entries) {
      LocalDateTime studiedAt = entry.getStudiedAt();
      List<StudyEntryTopic> links = entry.getStudyEntryTopics();
      if (links == null) continue;

      for (StudyEntryTopic link : links) {
        if (link.getTopic() == null || link.getTopic().getSubject() == null) continue;

        String subjectName = link.getTopic().getSubject().getName();
        String topicName = link.getTopic().getName();
        boolean priority = Boolean.TRUE.equals(link.getIsPriority());

        subjectTopicMap
            .computeIfAbsent(subjectName, s -> new LinkedHashMap<>())
            .computeIfAbsent(topicName, t -> new TopicAccumulator())
            .accumulate(priority, studiedAt);
      }
    }

    List<SubjectSummary> subjects = new ArrayList<>();
    for (Map.Entry<String, Map<String, TopicAccumulator>> subjEntry : subjectTopicMap.entrySet()) {
      String subject = subjEntry.getKey();
      Map<String, TopicAccumulator> topicStats = subjEntry.getValue();

      List<TopicSummary> topics = topicStats.entrySet().stream()
          .map(e -> TopicSummary.builder()
              .name(e.getKey())
              .count(e.getValue().count)
              .isPriority(e.getValue().anyPriority)
              .lastStudiedAt(e.getValue().lastStudiedAt)
              .build())
          .sorted(Comparator
              .comparing(TopicSummary::getLastStudiedAt, Comparator.nullsLast(Comparator.reverseOrder()))
              .thenComparing(TopicSummary::getName))
          .toList();

      int totalStudies = topics.stream().mapToInt(TopicSummary::getCount).sum();

      subjects.add(SubjectSummary.builder()
          .subject(subject)
          .topics(topics)
          .totalStudies(totalStudies)
          .build());
    }

    subjects.sort(Comparator
        .comparing(SubjectSummary::getTotalStudies, Comparator.reverseOrder())
        .thenComparing(SubjectSummary::getSubject));

    DateRange range = null;
    if (start != null && end != null) {
      range = DateRange.builder()
          .startDate(start.toString())
          .endDate(end.toString())
          .build();
    }

    return KnowledgeMapResponse.builder()
        .dateRange(range)
        .subjects(subjects)
        .build();
  }

  private static class TopicAccumulator {
    int count = 0;
    boolean anyPriority = false;
    LocalDateTime lastStudiedAt = null;

    void accumulate(boolean isPriority, LocalDateTime studiedAt) {
      count++;
      anyPriority = anyPriority || isPriority;
      if (studiedAt != null && (lastStudiedAt == null || studiedAt.isAfter(lastStudiedAt))) {
        lastStudiedAt = studiedAt;
      }
    }
  }
}
