package com.knowtify.service;

import com.knowtify.dto.StudyDtos;
import com.knowtify.dto.StudyDtos.*;
import com.knowtify.entity.*;
import com.knowtify.repository.*;
import com.knowtify.service.GeminiParsingService.ParsedTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyEntryService {

  private final StudyEntryRepository studyEntryRepository;
  private final TopicRepository topicRepository;
  private final StudyEntryTopicRepository studyEntryTopicRepository;
  private final UserRepository userRepository;
  private final GeminiParsingService geminiParsingService;
  private final SubjectService subjectService;

  @Transactional
  public CreateEntryResponse createStudyEntry(UUID userId, CreateEntryRequest request) {
    // Verify user exists
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    // Parse sentence with Gemini
    List<ParsedTopic> parsedTopics = geminiParsingService.parseStudyEntry(request.getSentence());
    
    // Create study entry
    StudyEntry studyEntry = StudyEntry.builder()
        .user(user)
        .originalSentence(request.getSentence())
        .studiedAt(LocalDateTime.now())
        .build();
    
    studyEntry = studyEntryRepository.save(studyEntry);
    
    // Process each parsed topic
    List<ParsedTopicDto> responseTopics = new ArrayList<>();
    
    for (ParsedTopic parsedTopic : parsedTopics) {
      try {
        // Find or create subject
        Subject subject = subjectService.findOrCreateSubject(parsedTopic.subject());
        
        // Find or create topic
        Topic topic = topicRepository.findByNameIgnoreCaseAndSubject_Id(
            parsedTopic.topic(), subject.getId())
            .orElseGet(() -> {
              Topic newTopic = Topic.builder()
                  .name(parsedTopic.topic())
                  .subject(subject)
                  .confidenceScore(parsedTopic.confidence())
                  .build();
              return topicRepository.save(newTopic);
            });
        
        // Create study entry topic link
        StudyEntryTopic studyEntryTopic = StudyEntryTopic.builder()
            .studyEntry(studyEntry)
            .topic(topic)
            .isPriority(parsedTopic.priority())
            .build();
        
        studyEntryTopicRepository.save(studyEntryTopic);
        
        // Add to response
        responseTopics.add(ParsedTopicDto.builder()
            .name(parsedTopic.topic())
            .subject(parsedTopic.subject())
            .isPriority(parsedTopic.priority())
            .reason(parsedTopic.reason())
            .confidence(parsedTopic.confidence())
            .build());
            
      } catch (Exception e) {
        log.error("Failed to process topic: {}", parsedTopic.topic(), e);
        // Continue processing other topics
      }
    }
    
    return CreateEntryResponse.builder()
        .message("Study entry created successfully")
        .entryId(studyEntry.getId())
        .studiedAt(studyEntry.getStudiedAt())
        .parsedTopics(responseTopics)
        .build();
  }

  public List<StudyDtos.EntryView> findRecent(UUID userId, int limit) {
    Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 100)), Sort.by(Sort.Direction.DESC, "studiedAt"));
    Page<StudyEntry> page = studyEntryRepository.findByUser_Id(userId, pageable);
    return page.getContent().stream().map(this::toEntryView).toList();
  }

  private StudyDtos.EntryView toEntryView(StudyEntry e) {
    List<StudyEntryTopic> links = Optional.ofNullable(e.getStudyEntryTopics()).orElse(Collections.emptyList());
    return StudyDtos.EntryView.builder()
            .id(e.getId())
            .originalSentence(e.getOriginalSentence())
            .studiedAt(e.getStudiedAt())
            .topics(
                    links.stream()
                            .filter(l -> l.getTopic() != null)
                            .map(l -> StudyDtos.TopicSummary.builder()
                                    .name(l.getTopic().getName())
                                    .count(1)
                                    .isPriority(Boolean.TRUE.equals(l.getIsPriority()))
                                    .lastStudiedAt(e.getStudiedAt())
                                    .build())
                            .toList()
            )
            .build();
  }
  @Transactional(readOnly = true)
  public List<StudyEntry> findRecentEntriesForUser(UUID userId, int limit) {
    return studyEntryRepository.findTop10ByUser_IdOrderByStudiedAtDesc(userId);
  }

}
