// ThresholdService.java
package com.example.caloriesCalculator.service;

import com.example.caloriesCalculator.repository.FoodEntryRepository;
import com.example.caloriesCalculator.entity.FoodEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ThresholdService {

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    private static final int DAILY_CALORIE_LIMIT = 2500;

    public boolean isThresholdReached(int currentCalories, int newCalories) {
        return (currentCalories + newCalories) > DAILY_CALORIE_LIMIT;
    }

    public int calculateDailyCalories(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return foodEntryRepository.findByUserIdAndDateTimeBetween(userId, startOfDay, endOfDay)
                .stream().mapToInt(FoodEntry::getCalorieValue).sum();
    }

    public boolean isThresholdExceeded(Long userId, LocalDate date) {
        return calculateDailyCalories(userId, date) > 2500;
    }
}
