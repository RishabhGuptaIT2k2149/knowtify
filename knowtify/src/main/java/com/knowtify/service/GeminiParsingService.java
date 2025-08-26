package com.knowtify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiParsingService {

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String model;

  public GeminiParsingService(
          @Value("${gemini.api.key}") String apiKey,
          @Value("${gemini.api.model:gemini-2.0-flash-exp}") String model,
          @Value("${gemini.api.base-url}") String baseUrl,
          ObjectMapper objectMapper
  ) {
    this.apiKey = apiKey;
    this.model = model;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
  }

  public static record ParsedTopic(
          String topic,
          String subject,
          boolean priority,
          String reason,
          double confidence
  ) {}

  public List<ParsedTopic> parseStudyEntry(String sentence) {
    try {
      String prompt = createAnalysisPrompt(sentence);
      String response = callGeminiAPI(prompt);
      return parseGeminiResponse(response);
    } catch (Exception e) {
      log.error("Gemini parsing failed for: '{}', falling back to simple parsing", sentence, e);
      return fallbackParsing(sentence);
    }
  }

  private String createAnalysisPrompt(String sentence) {
    return "Analyze this study entry and extract learning topics with their academic subjects.\n\n" +
            "Input: \"" + sentence + "\"\n\n" +
            "Instructions:\n" +
            "1. Extract specific topics/concepts studied\n" +
            "2. Categorize each topic under the most appropriate CS subject\n" +
            "3. Determine if topic is priority (if user struggled/found difficult/confusing/spent extra time)\n" +
            "4. Provide confidence score (0.1-1.0)\n\n" +
            "Available subjects: Data Structures & Algorithms, Operating Systems, Web Development, Database Systems, Machine Learning, Computer Networks, Software Engineering, Mobile Development, DevOps, Cybersecurity, Programming Languages, System Design, Mathematics, Computer Graphics, Other\n\n" +
            "Respond with ONLY valid JSON array:\n" +
            "[{\"topic\":\"specific topic name\",\"subject\":\"exact subject from list above\",\"priority\":true/false,\"reason\":\"why priority or not\",\"confidence\":0.85}]\n\n" +
            "Example input: \"I struggled with React hooks today and also learned quicksort\"\n" +
            "Example output: [{\"topic\":\"React hooks\",\"subject\":\"Web Development\",\"priority\":true,\"reason\":\"user struggled with concept\",\"confidence\":0.9},{\"topic\":\"quicksort algorithm\",\"subject\":\"Data Structures & Algorithms\",\"priority\":false,\"reason\":\"regular learning\",\"confidence\":0.85}]";
  }

  private String callGeminiAPI(String prompt) {
    try {
      var requestBody = Map.of(
              "contents", List.of(
                      Map.of("parts", List.of(
                              Map.of("text", prompt)
                      ))
              ),
              "generationConfig", Map.of(
                      "temperature", 0.1,
                      "maxOutputTokens", 1000
              )
      );

      String response = webClient.post()
              .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
              .bodyValue(requestBody)
              .retrieve()
              .bodyToMono(String.class)
              .block();

      return extractContentFromResponse(response);
    } catch (Exception e) {
      log.error("Failed to call Gemini API", e);
      throw new RuntimeException("Gemini API call failed", e);
    }
  }

  private String extractContentFromResponse(String response) throws JsonProcessingException {
    JsonNode root = objectMapper.readTree(response);
    // candidates[0].content.parts[0].text
    JsonNode textNode = root.path("candidates")
            .path(0)
            .path("content")
            .path("parts")
            .path(0)
            .path("text");

    if (textNode.isMissingNode() || textNode.isNull()) {
      throw new IllegalStateException("No text found in Gemini response");
    }
    return textNode.asText();
  }

  private List<ParsedTopic> parseGeminiResponse(String response) {
    try {
      String jsonPart = response == null ? "" : response.trim();

      // Strip Markdown code fences if the model added them:
      if (jsonPart.startsWith("```")) {
        jsonPart = jsonPart.substring(3).trim(); // remove opening ```
        if (jsonPart.startsWith("json")) {
          jsonPart = jsonPart.substring(4).trim(); // remove optional 'json'
        }
      }
      if (jsonPart.endsWith("```")) {
        jsonPart = jsonPart.substring(0, jsonPart.length() - 3).trim(); // remove closing ```
      }

      // If it's not starting with '[', try to extract the array portion
      if (!jsonPart.startsWith("[")) {
        int lb = jsonPart.indexOf('[');
        int rb = jsonPart.lastIndexOf(']');
        if (lb >= 0 && rb >= lb) {
          jsonPart = jsonPart.substring(lb, rb + 1).trim();
        }
      }

      JsonNode topics = objectMapper.readTree(jsonPart);
      if (!topics.isArray()) {
        throw new IllegalArgumentException("Model did not return a JSON array");
      }

      List<ParsedTopic> result = new ArrayList<>();
      for (JsonNode topicNode : topics) {
        result.add(new ParsedTopic(
                topicNode.path("topic").asText(""),
                topicNode.path("subject").asText("Other"),
                topicNode.path("priority").asBoolean(false),
                topicNode.path("reason").asText(""),
                topicNode.path("confidence").isNumber() ? topicNode.get("confidence").asDouble() : 0.5
        ));
      }
      return result;

    } catch (Exception e) {
      log.error("Failed to parse Gemini response: {}", response, e);
      throw new RuntimeException("Failed to parse Gemini response", e);
    }
  }

  private List<ParsedTopic> fallbackParsing(String sentence) {
    List<ParsedTopic> result = new ArrayList<>();

    String cleaned = sentence.toLowerCase()
            .replaceAll("^(i\\s+studied|studied|i\\s+learned|learned)\\s*", "")
            .trim();

    for (String segment : cleaned.split(",")) {
      String topic = segment.trim();
      if (topic.isEmpty()) continue;

      boolean isPriority = topic.startsWith("!");
      if (isPriority) topic = topic.substring(1).trim();

      result.add(new ParsedTopic(
              topic,
              "Other",
              isPriority,
              isPriority ? "marked with !" : "regular entry",
              0.3
      ));
    }

    return result;
  }
}
