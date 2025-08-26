package com.knowtify.controller;

import com.knowtify.config.JwtAuthFilter.AuthenticatedUser;
import com.knowtify.dto.StudyDtos.WeeklyReportResponse;
import com.knowtify.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/weekly")
  public ResponseEntity<WeeklyReportResponse> getWeeklyReport(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Integer week) {
    
    try {
      WeeklyReportResponse report = reportService.generateWeeklyReport(user.userId(), year, week);
      return ResponseEntity.ok(report);
    } catch (Exception e) {
      // In production, use proper error handling
      return ResponseEntity.internalServerError().build();
    }
  }
}
