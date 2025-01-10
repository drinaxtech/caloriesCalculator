// AdminReport.java
package com.example.caloriesCalculator.dto;

import com.example.caloriesCalculator.entity.User;

import java.util.List;

public class AdminReport {

    private long totalEntries;
    private double averageCalories;
    private List<User> overspendingUsers;

    public AdminReport(long totalEntries, double averageCalories, List<User> overspendingUsers) {
        this.totalEntries = totalEntries;
        this.averageCalories = averageCalories;
        this.overspendingUsers = overspendingUsers;
    }

    // Getters and Setters
}