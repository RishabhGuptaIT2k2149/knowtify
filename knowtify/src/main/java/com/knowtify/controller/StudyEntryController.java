package com.knowtify.controller;

import com.knowtify.config.JwtAuthFilter.AuthenticatedUser;
import com.knowtify.dto.StudyDtos;
import com.knowtify.dto.StudyDtos.*;
import com.knowtify.service.StudyEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
public class StudyEntryController {

  private final StudyEntryService studyEntryService;

  @PostMapping
  public ResponseEntity<CreateEntryResponse> createEntry(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody CreateEntryRequest request) {
    
    try {
      CreateEntryResponse response = studyEntryService.createStudyEntry(user.userId(), request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(CreateEntryResponse.builder()
              .message("Failed to create study entry: " + e.getMessage())
              .build());
    }

  }
//  @GetMapping("/api/v1/entries")
//  public ResponseEntity<Object> listRecentEntries(
//          @AuthenticationPrincipal AuthenticatedUser user,
//          @RequestParam(defaultValue = "20") int limit
//  ) {
//    if (user == null) return ResponseEntity.status(401).build();
//    List<EntryView> items = studyEntryService.findRecent(user.userId(), limit);
//    return ResponseEntity.ok(items);
//  }
// In StudyEntryController.java
@GetMapping
public ResponseEntity<List<StudyDtos.EntryView>> listRecentEntries(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam(defaultValue = "20") int limit
) {
  if (user == null) {
    return ResponseEntity.status(401).build();
  }

  System.out.println("📋 Getting recent entries for user: " + user.userId() + ", limit: " + limit);

  List<StudyDtos.EntryView> items = studyEntryService.findRecent(user.userId(), limit);

  System.out.println("📊 Found " + items.size() + " recent entries");

  return ResponseEntity.ok(items);
}


}
