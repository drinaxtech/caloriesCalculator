// UserService.java
package com.example.caloriesCalculator.service;

import com.example.caloriesCalculator.entity.User;
import com.example.caloriesCalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user, ensuring their email is unique and password is securely encoded.
     *
     * @param user The user entity to register.
     * @return The saved User entity.
     */
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    /**
     * Fetches a user by email.
     *
     * @param email The email of the user to fetch.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Updates a user's information.
     *
     * @param user The user entity with updated information.
     * @return The updated User entity.
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
