package com.knowtify.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class TopicParsingService {

  private static final Pattern LEADING_PHRASE = Pattern.compile("^(i\\s+studied|studied|i\\s+learned|learned)\\s*!?", Pattern.CASE_INSENSITIVE);

  public static record ParsedTopic(String name, boolean isPriority) {}

  public List<ParsedTopic> parseSentence(String sentence) {
    if (sentence == null || sentence.trim().isEmpty()) {
      return List.of();
    }

    String cleaned = LEADING_PHRASE.matcher(sentence).replaceFirst("").trim();

    Map<String, Boolean> dedup = new LinkedHashMap<>();
    for (String segment : cleaned.split(",")) {
      String s = segment == null ? "" : segment.trim();
      if (s.isEmpty()) continue;

      boolean isPriority = s.startsWith("!");
      if (isPriority) s = s.substring(1);

      String normalized = s.trim().toLowerCase();
      if (normalized.isEmpty()) continue;

      // Deduplicate, but if any occurrence marked priority, keep priority true
      dedup.merge(normalized, isPriority, (oldVal, newVal) -> oldVal || newVal);
    }

    List<ParsedTopic> result = new ArrayList<>();
    dedup.forEach((name, prio) -> result.add(new ParsedTopic(name, prio)));
    return result;
  }
}
