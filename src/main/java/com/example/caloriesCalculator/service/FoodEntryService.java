// FoodEntryService.java
package com.example.caloriesCalculator.service;

import com.example.caloriesCalculator.entity.FoodEntry;
import com.example.caloriesCalculator.repository.FoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FoodEntryService {

    private static final int MAX_MONTHLY_EXPENDITURE = 1000;

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    public List<FoodEntry> getAllFoodEntries() {
        return foodEntryRepository.findAll();
    }

    public FoodEntry addFoodEntry(FoodEntry foodEntry) {
        return foodEntryRepository.save(foodEntry);
    }

    public List<FoodEntry> filterEntries(LocalDate startDate, LocalDate endDate) {
        return foodEntryRepository.findByUserIdAndDateTimeBetween(1L, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    public Optional<FoodEntry> getFoodEntryById(Long id) {
        return foodEntryRepository.findById(id);
    }

    public FoodEntry updateFoodEntry(Long id, FoodEntry updatedFoodEntry) {
        FoodEntry existingEntry = foodEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food entry not found with id: " + id));

        existingEntry.setDateTime(updatedFoodEntry.getDateTime());
        existingEntry.setFoodName(updatedFoodEntry.getFoodName());
        existingEntry.setCalorieValue(updatedFoodEntry.getCalorieValue());
        existingEntry.setPrice(updatedFoodEntry.getPrice());

        return foodEntryRepository.save(existingEntry);
    }

    public List<FoodEntry> getEntriesForUserAndDate(Long userId, LocalDate date) {
        return foodEntryRepository.findByUserIdAndDateTimeBetween(
                userId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }

    // Method to get food entries by user ID
    public List<FoodEntry> getFoodEntriesByUserId(Long userId) {
        return foodEntryRepository.findByUserId(userId);
    }

    // Method to generate weekly summary report
    public Map<String, Object> getWeeklySummary(Long userId, int calorieThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1); // Start of the week
        LocalDate weekEnd = weekStart.plusDays(6); // End of the week

        // Fetch food entries for the current week
        List<FoodEntry> weeklyEntries = foodEntryRepository.findByUserIdAndDateTimeBetween(
                userId,
                weekStart.atStartOfDay(),
                weekEnd.plusDays(1).atStartOfDay()
        );


        // Group calories by day
        Map<LocalDate, Integer> caloriesPerDay = weeklyEntries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getDateTime().toLocalDate(),
                        Collectors.summingInt(FoodEntry::getCalorieValue)
                ));

        // Count the number of days the calorie threshold was exceeded
        long daysThresholdExceeded = caloriesPerDay.values().stream()
                .filter(calories -> calories > calorieThreshold)
                .count();

        // Calculate total expenditure for the week
        double totalExpenditure = weeklyEntries.stream()
                .mapToDouble(FoodEntry::getPrice)
                .sum();

        // Prepare the summary report
        Map<String, Object> weeklySummary = new HashMap<>();
        weeklySummary.put("caloriesPerDay", caloriesPerDay);
        weeklySummary.put("daysThresholdExceeded", daysThresholdExceeded);
        weeklySummary.put("totalExpenditure", totalExpenditure);

        return weeklySummary;
    }

    public List<FoodEntry> getFoodEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return foodEntryRepository.findByDateTimeBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
    }

    public List<FoodEntry> getFoodEntriesByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return foodEntryRepository.findByUserIdAndDateTimeBetween(
                userId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
    }

    public Map<String, Object> getEntriesReportForLast7Days(LocalDate today) {
        LocalDate weekStart = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<FoodEntry> last7DaysEntries = foodEntryRepository.findByDateTimeBetween(
                today.minusDays(6).atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        long lastWeekCount = foodEntryRepository.countByDateTimeBetween(
                weekStart.atStartOfDay(),
                weekEnd.atStartOfDay()
        );

        return Map.of(
                "last7DaysCount", last7DaysEntries.size(),
                "lastWeekCount", lastWeekCount
        );
    }

    public double getAverageCaloriesPerUserForLast7Days(LocalDate today) {
        LocalDate last7DaysStart = today.minusDays(6);
        List<FoodEntry> last7DaysEntries = foodEntryRepository.findByDateTimeBetween(
                last7DaysStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        Map<Long, List<FoodEntry>> entriesGroupedByUser = last7DaysEntries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getUser().getId()));

        return entriesGroupedByUser.values().stream()
                .mapToDouble(userEntries -> userEntries.stream().mapToInt(FoodEntry::getCalorieValue).average().orElse(0))
                .average()
                .orElse(0);
    }

    public List<String> getUsersExceedingMonthlyPriceLimit(YearMonth lastMonth) {
        LocalDate monthStart = lastMonth.atDay(1);
        LocalDate monthEnd = lastMonth.atEndOfMonth();

        return foodEntryRepository.findUsersExceedingPriceLimit(
                monthStart.atStartOfDay(),
                monthEnd.plusDays(1).atStartOfDay(),
                MAX_MONTHLY_EXPENDITURE
        );
    }

    public void deleteFoodEntryById(Long id) {
        if (foodEntryRepository.existsById(id)) {
            foodEntryRepository.deleteById(id);
        } else {
            throw new RuntimeException("Food entry not found for ID: " + id);
        }
    }

}