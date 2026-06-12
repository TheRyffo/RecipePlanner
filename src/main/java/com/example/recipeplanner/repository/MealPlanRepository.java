package com.example.recipeplanner.repository;

import com.example.recipeplanner.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    List<MealPlan> findByUserIdAndPlannedDateBetween(Long userId, LocalDate from, LocalDate to);

    List<MealPlan> findByUserIdAndPlannedDate(Long userId, LocalDate date);

    List<MealPlan> findByUserId(Long userId);

    void deleteByUserIdAndPlannedDateBefore(Long userId, LocalDate date);
}
