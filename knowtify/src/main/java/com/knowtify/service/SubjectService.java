package com.knowtify.service;

import com.knowtify.entity.Subject;
import com.knowtify.repository.SubjectRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

  private final SubjectRepository subjectRepository;

  private static final List<String> DEFAULT_SUBJECTS = List.of(
    "Data Structures & Algorithms",
    "Operating Systems", 
    "Web Development",
    "Database Systems",
    "Machine Learning",
    "Computer Networks",
    "Software Engineering",
    "Mobile Development",
    "DevOps",
    "Cybersecurity",
    "Programming Languages",
    "System Design",
    "Mathematics",
    "Computer Graphics",
    "Other"
  );

  @PostConstruct
  @Transactional
  public void seedSubjects() {
    for (String subjectName : DEFAULT_SUBJECTS) {
      if (!subjectRepository.findByNameIgnoreCase(subjectName).isPresent()) {
        Subject subject = Subject.builder()
            .name(subjectName)
            .description("Computer Science subject: " + subjectName)
            .build();
        subjectRepository.save(subject);
        log.debug("Seeded subject: {}", subjectName);
      }
    }
  }

  @Transactional(readOnly = true)
  public Optional<Subject> findByName(String name) {
    return subjectRepository.findByNameIgnoreCase(name.trim());
  }

  @Transactional
  public Subject findOrCreateSubject(String name) {
    String cleanName = name.trim();
    
    return subjectRepository.findByNameIgnoreCase(cleanName)
        .orElseGet(() -> {
          Subject newSubject = Subject.builder()
              .name(cleanName)
              .description("Subject: " + cleanName)
              .build();
          Subject saved = subjectRepository.save(newSubject);
          log.info("Created new subject: {}", cleanName);
          return saved;
        });
  }

  @Transactional(readOnly = true)
  public List<Subject> findAll() {
    return subjectRepository.findAll();
  }
}
