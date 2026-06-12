package com.example.recipeplanner.service.impl;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.MealPlan;
import com.example.recipeplanner.entity.Recipe;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.exception.AccessDeniedException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.MealPlanRepository;
import com.example.recipeplanner.repository.RecipeRepository;
import com.example.recipeplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public DomainDtos.MealPlanResponse create(DomainDtos.MealPlanRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + request.getRecipeId()));

        MealPlan mealPlan = MealPlan.builder()
                .user(user)
                .recipe(recipe)
                .plannedDate(request.getPlannedDate())
                .mealType(request.getMealType())
                .servings(request.getServings())
                .notes(request.getNotes())
                .build();

        return mapToResponse(mealPlanRepository.save(mealPlan));
    }

    @Transactional(readOnly = true)
    public List<DomainDtos.MealPlanResponse> getWeekPlan(LocalDate from, LocalDate to) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return mealPlanRepository.findByUserIdAndPlannedDateBetween(user.getId(), from, to)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DomainDtos.MealPlanResponse> getDayPlan(LocalDate date) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return mealPlanRepository.findByUserIdAndPlannedDate(user.getId(), date)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!mealPlan.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your meal plan");
        }
        mealPlanRepository.delete(mealPlan);
    }

    @Transactional
    public DomainDtos.MealPlanResponse update(Long id, DomainDtos.MealPlanRequest request) {
        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!mealPlan.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your meal plan");
        }

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        mealPlan.setRecipe(recipe);
        mealPlan.setPlannedDate(request.getPlannedDate());
        mealPlan.setMealType(request.getMealType());
        mealPlan.setServings(request.getServings());
        mealPlan.setNotes(request.getNotes());

        return mapToResponse(mealPlanRepository.save(mealPlan));
    }

    private DomainDtos.MealPlanResponse mapToResponse(MealPlan mp) {
        DomainDtos.MealPlanResponse dto = new DomainDtos.MealPlanResponse();
        dto.setId(mp.getId());
        dto.setRecipeId(mp.getRecipe().getId());
        dto.setRecipeTitle(mp.getRecipe().getTitle());
        dto.setPlannedDate(mp.getPlannedDate());
        dto.setMealType(mp.getMealType());
        dto.setServings(mp.getServings());
        dto.setNotes(mp.getNotes());
        return dto;
    }
}
