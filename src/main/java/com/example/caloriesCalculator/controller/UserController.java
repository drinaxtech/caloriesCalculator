// Updated UserController.java
package com.example.caloriesCalculator.controller;

import com.example.caloriesCalculator.entity.User;
import com.example.caloriesCalculator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        System.out.println(user);
        try {
            userService.registerUser(user);
            model.addAttribute("success", "User registered successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
        }
        return "register";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "username", required = false) String email, @RequestParam(value = "password", required = false) String password, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

        if (email != null && password != null) {
            try {
                User user = userService.findUserByEmail(email).orElse(null);
                if (user == null || !userService.verifyPassword(password, user.getPassword())) {
                    model.addAttribute("error", "Invalid username or password");
                    //model.addAttribute("error", password);
                    return "login";
                }

                if ("ADMIN".equals(user.getRole())) {
                    // Simulate authentication success for redirection
                    SecurityContextHolder.getContext().setAuthentication(null); // Spring will handle real auth context
                    return "redirect:/admin/dashboard";
                } else {
                    model.addAttribute("error", "USER");
                    return "login";
                }
            } catch (Exception e) {
                model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
                return "login";
            }
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            User user = userService.findUserByEmail(username).orElse(null);
            if (user == null || !userService.verifyPassword(password, user.getPassword())) {
                //model.addAttribute("error", "Invalid username or password");
                model.addAttribute("error", password);
                return "login";
            }

            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "redirect:/users/login?logout=true";
    }
}