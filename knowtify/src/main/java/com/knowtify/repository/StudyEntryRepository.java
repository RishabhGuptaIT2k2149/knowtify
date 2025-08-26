package com.knowtify.repository;

import com.knowtify.entity.StudyEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyEntryRepository extends JpaRepository<StudyEntry, UUID> {

  List<StudyEntry> findTop10ByUser_IdOrderByStudiedAtDesc(UUID userId);

//  List<StudyEntry> findByUser_IdAndStudiedAtBetween(
//          UUID userId,
//          LocalDateTime startDate,
//          LocalDateTime endDate);   replaced with to and from
  Page<StudyEntry> findByUser_Id(UUID userId, Pageable pageable);

  List<StudyEntry> findByUser_IdAndStudiedAtBetween(UUID userId, LocalDateTime from, LocalDateTime to);
  @Query("SELECT se FROM StudyEntry se " +
          "LEFT JOIN FETCH se.studyEntryTopics set " +
          "LEFT JOIN FETCH set.topic t " +
          "LEFT JOIN FETCH t.subject " +
          "WHERE se.user.id = :userId " +
          "AND se.studiedAt BETWEEN :startDate AND :endDate " +
          "ORDER BY se.studiedAt DESC")
  List<StudyEntry> findByUserAndDateRangeWithTopics(
          @Param("userId") UUID userId,
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);
}
