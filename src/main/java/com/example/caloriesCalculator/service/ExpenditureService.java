// ExpenditureService.java
package com.example.caloriesCalculator.service;

import com.example.caloriesCalculator.repository.FoodEntryRepository;
import com.example.caloriesCalculator.entity.FoodEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.LocalDateTime;

@Service
public class ExpenditureService {

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    private static final int MAX_MONTHLY_EXPENDITURE = 1000;

    public double calculateMonthlyExpenditure(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atStartOfDay();
        return foodEntryRepository.findByUserIdAndDateTimeBetween(userId, start, end)
                .stream().mapToDouble(FoodEntry::getPrice).sum();
    }

    public boolean isExpenditureExceeded(Long userId, YearMonth month) {
        return calculateMonthlyExpenditure(userId, month) > MAX_MONTHLY_EXPENDITURE;
    }
}