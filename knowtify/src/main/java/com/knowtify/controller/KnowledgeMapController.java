package com.knowtify.controller;

import com.knowtify.config.JwtAuthFilter.AuthenticatedUser;
import com.knowtify.dto.StudyDtos.KnowledgeMapResponse;
import com.knowtify.service.KnowledgeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge-map")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeMapController {

  private final KnowledgeMapService knowledgeMapService;

  @GetMapping
  public ResponseEntity<KnowledgeMapResponse> getKnowledgeMap(
          @AuthenticationPrincipal AuthenticatedUser user,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    log.info("KM user={}", user != null ? user.userId() : null);

    if (user == null) {
      return ResponseEntity.status(401).build();
    }

    // If only one is provided, return 400 to avoid ambiguous ranges
    if ((startDate == null) != (endDate == null)) {
      return ResponseEntity.badRequest().build();
    }
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      return ResponseEntity.badRequest().build();
    }

    KnowledgeMapResponse resp =
            knowledgeMapService.getKnowledgeMap(user.userId(), startDate, endDate);
    return ResponseEntity.ok(resp);
  }
}

