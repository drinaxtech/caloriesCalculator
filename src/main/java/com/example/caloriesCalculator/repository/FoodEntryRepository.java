// FoodEntryRepository.java
package com.example.caloriesCalculator.repository;

import com.example.caloriesCalculator.entity.FoodEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {
    List<FoodEntry> findByUserIdAndDateTimeBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<FoodEntry> findByUserId(Long userId);

    List<FoodEntry> findByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM FoodEntry e WHERE e.dateTime BETWEEN :startDateTime AND :endDateTime")
    long countByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT DISTINCT e.user.email FROM FoodEntry e " +
            "WHERE e.dateTime BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY e.user " +
            "HAVING SUM(e.price) > :priceLimit")
    List<String> findUsersExceedingPriceLimit(LocalDateTime startDateTime, LocalDateTime endDateTime, double priceLimit);
}
