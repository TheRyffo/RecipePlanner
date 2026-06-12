package com.example.recipeplanner.dto;

import com.example.recipeplanner.entity.MealPlan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DomainDtos {

    // ==================== INGREDIENT ====================
    @Data
    public static class IngredientRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Unit is required")
        private String unit;

        private BigDecimal caloriesPerUnit;
    }

    @Data
    public static class IngredientResponse {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal caloriesPerUnit;
    }

    // ==================== CATEGORY ====================
    @Data
    public static class CategoryRequest {
        @NotBlank(message = "Name is required")
        private String name;

        private String description;
    }

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private String createdByUsername;
    }

    // ==================== MEAL PLAN ====================
    @Data
    public static class MealPlanRequest {
        @NotNull(message = "Recipe ID is required")
        private Long recipeId;

        @NotNull(message = "Planned date is required")
        private LocalDate plannedDate;

        @NotNull(message = "Meal type is required")
        private MealPlan.MealType mealType;

        @Min(value = 1)
        private Integer servings = 1;

        private String notes;
    }

    @Data
    public static class MealPlanResponse {
        private Long id;
        private Long recipeId;
        private String recipeTitle;
        private LocalDate plannedDate;
        private MealPlan.MealType mealType;
        private Integer servings;
        private String notes;
    }

    // ==================== SHOPPING LIST ====================
    @Data
    public static class ShoppingListRequest {
        @NotNull(message = "Ingredient ID is required")
        private Long ingredientId;

        @NotNull
        @Min(value = 0)
        private BigDecimal quantity;
    }

    @Data
    public static class ShoppingListResponse {
        private Long id;
        private Long ingredientId;
        private String ingredientName;
        private String unit;
        private BigDecimal quantity;
        private boolean purchased;
        private LocalDateTime createdAt;
    }

    // ==================== USER ====================
    @Data
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private LocalDateTime createdAt;
    }
}
