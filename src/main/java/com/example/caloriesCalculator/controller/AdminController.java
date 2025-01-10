// AdminController.java
package com.example.caloriesCalculator.controller;

import com.example.caloriesCalculator.entity.User;
import com.example.caloriesCalculator.repository.UserRepository;
import com.example.caloriesCalculator.service.AdminService;
import com.example.caloriesCalculator.service.FoodEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    private FoodEntryService foodEntryService;
    private UserRepository userRepository;

    @Autowired
    public AdminController(FoodEntryService foodEntryService, UserRepository userRepository) {
        this.foodEntryService = foodEntryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/report")
    public String viewAdminReport(Model model) {
        LocalDate today = LocalDate.now();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String userRole = user.getRole();

        if(Objects.equals(userRole, "USER")) {
            return "redirect:/dashboard";
        }

        // Last 7 days and the week before

        Map<String, Object> last7DaysReport = foodEntryService.getEntriesReportForLast7Days(today);
        model.addAttribute("last7DaysReport", last7DaysReport);

        // Average calories per user for the last 7 days
        double avgCaloriesPerUser = foodEntryService.getAverageCaloriesPerUserForLast7Days(today);
        model.addAttribute("avgCaloriesPerUser", avgCaloriesPerUser);

        // Users exceeding the monthly price limit
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        List<String> usersExceedingPriceLimit = foodEntryService.getUsersExceedingMonthlyPriceLimit(lastMonth);
        model.addAttribute("usersExceedingPriceLimit", usersExceedingPriceLimit);

        return "admin_report";
    }

    @GetMapping("/expenditure-warning")
    public String showUsersExceedingExpenditure(Model model) {
        model.addAttribute("overspendingUsers", adminService.getUsersExceedingMonthlyExpenditure(YearMonth.now()));
        return "admin-expenditure-warning";
    }
}