package com.knowtify.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Component
public class WeekUtil {

  @Data
  @AllArgsConstructor
  public static class WeekRange {
    private LocalDate startDate;
    private LocalDate endDate;
  }

  public WeekRange getCurrentWeekRange() {
    LocalDate today = LocalDate.now();
    return getWeekRange(today.getYear(), today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
  }

  public WeekRange getWeekRange(int year, int weekNumber) {
    // ISO week starts on Monday
    LocalDate startOfWeek = LocalDate.of(year, 1, 1)
        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber)
        .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    
    LocalDate endOfWeek = startOfWeek.plusDays(6); // Sunday
    
    return new WeekRange(startOfWeek, endOfWeek);
  }

  public int getCurrentYear() {
    return LocalDate.now().getYear();
  }

  public int getCurrentWeekNumber() {
    return LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
  }
}
