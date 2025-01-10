package com.example.caloriesCalculator.controller;

import com.example.caloriesCalculator.entity.FoodEntry;
import com.example.caloriesCalculator.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.example.caloriesCalculator.service.FoodEntryService;
import com.example.caloriesCalculator.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private FoodEntryService foodEntryService;
    private UserRepository userRepository;

    @Autowired
    public DashboardController(FoodEntryService foodEntryService, UserRepository userRepository) {
        this.foodEntryService = foodEntryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get the authenticated username
        model.addAttribute("message", "Welcome " + username + " to your dashboard!");
        model.addAttribute("username", username);


        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String userRole = user.getRole();


        if(Objects.equals(userRole, "ADMIN")) {

            return "admin_dashboard";
        }

        return "dashboard";
    }

    @GetMapping("/weekly-report")
    public String showWeeklyReport(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Long userId = user.getId(); // Assuming the User object has an ID field
        int calorieThreshold = 2500; // Example threshold

        Map<String, Object> weeklySummary = foodEntryService.getWeeklySummary(userId, calorieThreshold);

        System.out.println("Weekly Summary: " + weeklySummary);

        model.addAttribute("caloriesPerDay", weeklySummary.get("caloriesPerDay"));
        model.addAttribute("daysThresholdExceeded", weeklySummary.get("daysThresholdExceeded"));
        model.addAttribute("totalExpenditure", weeklySummary.get("totalExpenditure"));

        return "weekly_report";
    }
}
