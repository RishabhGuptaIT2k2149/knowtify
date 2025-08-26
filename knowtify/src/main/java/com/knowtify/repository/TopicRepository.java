package com.knowtify.repository;

import com.knowtify.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
  Optional<Topic> findByNameIgnoreCaseAndSubject_Id(String name, UUID subjectId);
}
