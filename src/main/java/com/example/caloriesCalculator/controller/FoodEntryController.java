// FoodEntryController.java
package com.example.caloriesCalculator.controller;

import com.example.caloriesCalculator.entity.FoodEntry;
import com.example.caloriesCalculator.entity.User;
import com.example.caloriesCalculator.repository.UserRepository;
import com.example.caloriesCalculator.service.FoodEntryService;
import com.example.caloriesCalculator.service.ThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/food-entries")
public class FoodEntryController {


    private FoodEntryService foodEntryService;
    private UserRepository userRepository;

    @Autowired
    private ThresholdService thresholdService;


    @Autowired
    public FoodEntryController(FoodEntryService foodEntryService, UserRepository userRepository) {
        this.foodEntryService = foodEntryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listFoodEntries(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String userRole = user.getRole();

        if(Objects.equals(userRole, "ADMIN")) {
            model.addAttribute("filteredEntries", foodEntryService.getAllFoodEntries());
            System.out.println(foodEntryService.getAllFoodEntries());
            return "admin_food_entries";
        }

        model.addAttribute("filteredEntries", foodEntryService.getFoodEntriesByUserId(user.getId()));
        return "food_entries";
    }

    @GetMapping("/add")
    public String showAddFoodEntryForm(Model model) {
        model.addAttribute("foodEntry", new FoodEntry());
        return "food_entries_add";
    }

    @PostMapping("/add")
    public String addFoodEntry(@ModelAttribute FoodEntry foodEntry, Model model) {
        // Retrieve logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Calculate current daily calories
        List<FoodEntry> todayEntries = foodEntryService.getEntriesForUserAndDate(user.getId(), foodEntry.getDateTime().toLocalDate());
        //int currentCalories = todayEntries.stream().mapToInt(FoodEntry::getCalorieValue).sum();

        // Check if threshold is reached
        //if (thresholdService.isThresholdReached(currentCalories, foodEntry.getCalorieValue())) {
        //    model.addAttribute("error", "Calorie threshold exceeded. Please review your entry.");
        //    model.addAttribute("foodEntry", foodEntry);
        //    return "food_entries_add";
        //}

        // Assign the user to the food entry
        foodEntry.setUser(user);

        // Save the food entry
        foodEntryService.addFoodEntry(foodEntry);


        return "redirect:/food-entries";
    }

    @GetMapping("/edit/{id}")
    public String editFoodEntry(@PathVariable Long id, Model model) {
        FoodEntry foodEntry = foodEntryService.getFoodEntryById(id)
                .orElseThrow(() -> new RuntimeException("Food entry not found with id: " + id));
        model.addAttribute("foodEntry", foodEntry);
        return "food_entries_edit";
    }

    @PostMapping("/edit/{id}")
    public String updateFoodEntry(@PathVariable Long id, @ModelAttribute FoodEntry foodEntry) {
        foodEntryService.updateFoodEntry(id, foodEntry);
        return "redirect:/food-entries";
    }

    @GetMapping("/filter")
    public String filterFoodEntries(@RequestParam("fromDate") String fromDate,
                                    @RequestParam("toDate") String toDate,
                                    Model model) {
        LocalDate startDate = LocalDate.parse(fromDate);
        LocalDate endDate = LocalDate.parse(toDate);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String userRole = user.getRole();

        String filteredEntriesText = "Filtered entries from " + startDate + " to " + endDate;
        model.addAttribute("filteredEntriesText", filteredEntriesText);

        if(Objects.equals(userRole, "ADMIN")) {
            List<FoodEntry> filteredEntries = foodEntryService.getFoodEntriesByDateRange(startDate, endDate);
            model.addAttribute("filteredEntries", filteredEntries);

            return "admin_food_entries";
        }

        List<FoodEntry> filteredEntries = foodEntryService.getFoodEntriesByUserAndDateRange(user.getId(), startDate, endDate);
        model.addAttribute("filteredEntries", filteredEntries);


        return "food_entries"; // The name of the Thymeleaf template
    }

    @GetMapping("/delete/{id}")
    public String deleteFoodEntry(@PathVariable Long id, Model model) {
        foodEntryService.deleteFoodEntryById(id);
        model.addAttribute("success", "Deleted successfully");
        return "redirect:/food-entries";
    }
}
