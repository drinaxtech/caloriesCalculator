// AdminService.java
package com.example.caloriesCalculator.service;

import com.example.caloriesCalculator.dto.AdminReport;
import com.example.caloriesCalculator.entity.User;
import com.example.caloriesCalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenditureService expenditureService;

    /**
     * Generate a report for admin purposes, listing overspending users and statistics.
     */
    public AdminReport generateAdminReport() {
        YearMonth currentMonth = YearMonth.now();
        List<User> overspendingUsers = getUsersExceedingMonthlyExpenditure(currentMonth);
        long totalEntries = 0; // Replace with actual data logic
        double averageCalories = 0; // Replace with actual data logic

        return new AdminReport(totalEntries, averageCalories, overspendingUsers);
    }

    /**
     * Fetches all users who have exceeded the monthly expenditure limit.
     *
     * @param month The month for which to check the expenditures.
     * @return List of Users exceeding their expenditure limit.
     */
    public List<User> getUsersExceedingMonthlyExpenditure(YearMonth month) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> expenditureService.isExpenditureExceeded(user.getId(), month))
                .collect(Collectors.toList());
    }
}
