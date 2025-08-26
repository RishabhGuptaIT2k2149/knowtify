package com.knowtify.repository;

import com.knowtify.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
  Optional<Subject> findByNameIgnoreCase(String name);
}
