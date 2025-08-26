package com.knowtify.repository;

import com.knowtify.entity.StudyEntryTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudyEntryTopicRepository extends JpaRepository<StudyEntryTopic, UUID> {
}
