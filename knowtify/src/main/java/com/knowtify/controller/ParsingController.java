package com.knowtify.controller;

import com.knowtify.dto.StudyDtos.ParsedTopicDto;
import com.knowtify.dto.StudyDtos.StudyEntryRequest;
import com.knowtify.dto.StudyDtos.StudyEntryResponse;
import com.knowtify.service.TopicParsingService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dev")
public class ParsingController {

  private final TopicParsingService parsingService;

  public ParsingController(TopicParsingService parsingService) {
    this.parsingService = parsingService;
  }

  @PostMapping("/parse")
  public ResponseEntity<StudyEntryResponse> parse(@RequestBody @Validated StudyEntryRequest req) {
    var parsed = parsingService.parseSentence(req.getSentence()).stream()
      .map(p -> ParsedTopicDto.builder().name(p.name()).isPriority(p.isPriority()).build())
      .toList();
    return ResponseEntity.ok(
      StudyEntryResponse.builder()
        .message("Parsed successfully")
        .parsedTopics(parsed)
        .build()
    );
  }
}
