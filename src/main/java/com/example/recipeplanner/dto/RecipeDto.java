package com.example.recipeplanner.dto;

import com.example.recipeplanner.entity.Recipe;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RecipeDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotBlank(message = "Instructions are required")
        private String instructions;

        @Min(value = 1, message = "Cooking time must be positive")
        private Integer cookingTimeMinutes;

        @Min(value = 1, message = "Servings must be positive")
        private Integer servings;

        private Recipe.Difficulty difficulty;

        private String imageUrl;

        private Long categoryId;

        private List<IngredientItem> ingredients;

        @Data
        public static class IngredientItem {
            @NotNull
            private Long ingredientId;

            @NotNull
            @Min(value = 0)
            private BigDecimal quantity;

            private String notes;
        }
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private String instructions;
        private Integer cookingTimeMinutes;
        private Integer servings;
        private Recipe.Difficulty difficulty;
        private String imageUrl;
        private Long categoryId;
        private List<CreateRequest.IngredientItem> ingredients;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String instructions;
        private Integer cookingTimeMinutes;
        private Integer servings;
        private Recipe.Difficulty difficulty;
        private String imageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String authorUsername;
        private String categoryName;
        private List<RecipeIngredientDto> ingredients;
    }

    @Data
    public static class RecipeIngredientDto {
        private Long ingredientId;
        private String ingredientName;
        private String unit;
        private BigDecimal quantity;
        private String notes;
    }

    @Data
    public static class Summary {
        private Long id;
        private String title;
        private String description;
        private Integer cookingTimeMinutes;
        private Integer servings;
        private Recipe.Difficulty difficulty;
        private String categoryName;
        private String authorUsername;
    }
}
