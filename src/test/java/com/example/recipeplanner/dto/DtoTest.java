package com.example.recipeplanner.dto;

import com.example.recipeplanner.entity.MealPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    // ===== Покрываем конструкторы классов-обёрток =====

    @Test
    @DisplayName("AuthDto - outer class constructor")
    void authDtoOuterClass() {
        AuthDto dto = new AuthDto();
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("DomainDtos - outer class constructor")
    void domainDtosOuterClass() {
        DomainDtos dto = new DomainDtos();
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("RecipeDto - outer class constructor")
    void recipeDtoOuterClass() {
        RecipeDto dto = new RecipeDto();
        assertThat(dto).isNotNull();
    }

    // ===== AuthDto =====

    @Test
    @DisplayName("AuthDto.RegisterRequest - getters and setters")
    void authRegisterRequest() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@test.com");
        req.setPassword("pass123");

        assertThat(req.getUsername()).isEqualTo("alice");
        assertThat(req.getEmail()).isEqualTo("alice@test.com");
        assertThat(req.getPassword()).isEqualTo("pass123");
    }

    @Test
    @DisplayName("AuthDto.LoginRequest - getters and setters")
    void authLoginRequest() {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setUsername("bob");
        req.setPassword("secret");

        assertThat(req.getUsername()).isEqualTo("bob");
        assertThat(req.getPassword()).isEqualTo("secret");
    }

    @Test
    @DisplayName("AuthDto.AuthResponse - constructor and getters")
    void authResponse() {
        AuthDto.AuthResponse resp = new AuthDto.AuthResponse("token123", "alice", "alice@test.com");

        assertThat(resp.getToken()).isEqualTo("token123");
        assertThat(resp.getUsername()).isEqualTo("alice");
        assertThat(resp.getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("AuthDto.AuthResponse - setters")
    void authResponseSetters() {
        AuthDto.AuthResponse resp = new AuthDto.AuthResponse("t", "u", "e");
        resp.setToken("newtoken");
        resp.setUsername("newuser");
        resp.setEmail("new@test.com");

        assertThat(resp.getToken()).isEqualTo("newtoken");
        assertThat(resp.getUsername()).isEqualTo("newuser");
        assertThat(resp.getEmail()).isEqualTo("new@test.com");
    }

    // ===== DomainDtos =====

    @Test
    @DisplayName("DomainDtos.IngredientRequest - getters and setters")
    void ingredientRequest() {
        DomainDtos.IngredientRequest req = new DomainDtos.IngredientRequest();
        req.setName("Flour");
        req.setUnit("g");
        req.setCaloriesPerUnit(BigDecimal.valueOf(3.64));

        assertThat(req.getName()).isEqualTo("Flour");
        assertThat(req.getUnit()).isEqualTo("g");
        assertThat(req.getCaloriesPerUnit()).isEqualByComparingTo(BigDecimal.valueOf(3.64));
    }

    @Test
    @DisplayName("DomainDtos.IngredientResponse - getters and setters")
    void ingredientResponse() {
        DomainDtos.IngredientResponse resp = new DomainDtos.IngredientResponse();
        resp.setId(1L);
        resp.setName("Salt");
        resp.setUnit("g");
        resp.setCaloriesPerUnit(BigDecimal.ZERO);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getName()).isEqualTo("Salt");
        assertThat(resp.getUnit()).isEqualTo("g");
    }

    @Test
    @DisplayName("DomainDtos.CategoryRequest - getters and setters")
    void categoryRequest() {
        DomainDtos.CategoryRequest req = new DomainDtos.CategoryRequest();
        req.setName("Breakfast");
        req.setDescription("Morning meals");

        assertThat(req.getName()).isEqualTo("Breakfast");
        assertThat(req.getDescription()).isEqualTo("Morning meals");
    }

    @Test
    @DisplayName("DomainDtos.CategoryResponse - getters and setters")
    void categoryResponse() {
        DomainDtos.CategoryResponse resp = new DomainDtos.CategoryResponse();
        resp.setId(2L);
        resp.setName("Dinner");
        resp.setDescription("Evening meals");
        resp.setCreatedByUsername("admin");

        assertThat(resp.getId()).isEqualTo(2L);
        assertThat(resp.getName()).isEqualTo("Dinner");
        assertThat(resp.getCreatedByUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("DomainDtos.MealPlanRequest - getters and setters")
    void mealPlanRequest() {
        DomainDtos.MealPlanRequest req = new DomainDtos.MealPlanRequest();
        req.setRecipeId(1L);
        req.setPlannedDate(LocalDate.of(2025, 6, 1));
        req.setMealType(MealPlan.MealType.LUNCH);
        req.setServings(2);
        req.setNotes("Extra spicy");

        assertThat(req.getRecipeId()).isEqualTo(1L);
        assertThat(req.getPlannedDate()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(req.getMealType()).isEqualTo(MealPlan.MealType.LUNCH);
        assertThat(req.getServings()).isEqualTo(2);
        assertThat(req.getNotes()).isEqualTo("Extra spicy");
    }

    @Test
    @DisplayName("DomainDtos.MealPlanResponse - getters and setters")
    void mealPlanResponse() {
        DomainDtos.MealPlanResponse resp = new DomainDtos.MealPlanResponse();
        resp.setId(10L);
        resp.setRecipeId(5L);
        resp.setRecipeTitle("Pasta");
        resp.setPlannedDate(LocalDate.now());
        resp.setMealType(MealPlan.MealType.DINNER);
        resp.setServings(3);
        resp.setNotes("note");

        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getRecipeTitle()).isEqualTo("Pasta");
        assertThat(resp.getMealType()).isEqualTo(MealPlan.MealType.DINNER);
    }

    @Test
    @DisplayName("DomainDtos.ShoppingListRequest - getters and setters")
    void shoppingListRequest() {
        DomainDtos.ShoppingListRequest req = new DomainDtos.ShoppingListRequest();
        req.setIngredientId(3L);
        req.setQuantity(BigDecimal.valueOf(250));

        assertThat(req.getIngredientId()).isEqualTo(3L);
        assertThat(req.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    @DisplayName("DomainDtos.ShoppingListResponse - getters and setters")
    void shoppingListResponse() {
        DomainDtos.ShoppingListResponse resp = new DomainDtos.ShoppingListResponse();
        resp.setId(1L);
        resp.setIngredientId(2L);
        resp.setIngredientName("Milk");
        resp.setUnit("ml");
        resp.setQuantity(BigDecimal.valueOf(500));
        resp.setPurchased(true);
        resp.setCreatedAt(LocalDateTime.now());

        assertThat(resp.getIngredientName()).isEqualTo("Milk");
        assertThat(resp.isPurchased()).isTrue();
        assertThat(resp.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("DomainDtos.UserResponse - getters and setters")
    void userResponse() {
        DomainDtos.UserResponse resp = new DomainDtos.UserResponse();
        resp.setId(1L);
        resp.setUsername("alice");
        resp.setEmail("alice@test.com");
        resp.setCreatedAt(LocalDateTime.now());

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getUsername()).isEqualTo("alice");
        assertThat(resp.getEmail()).isEqualTo("alice@test.com");
    }

    // ===== RecipeDto =====

    @Test
    @DisplayName("RecipeDto.CreateRequest - getters and setters")
    void recipeCreateRequest() {
        RecipeDto.CreateRequest req = new RecipeDto.CreateRequest();
        req.setTitle("Pasta");
        req.setDescription("Italian dish");
        req.setInstructions("Cook it");
        req.setCookingTimeMinutes(20);
        req.setServings(2);
        req.setCategoryId(1L);

        assertThat(req.getTitle()).isEqualTo("Pasta");
        assertThat(req.getInstructions()).isEqualTo("Cook it");
        assertThat(req.getCookingTimeMinutes()).isEqualTo(20);
    }

    @Test
    @DisplayName("RecipeDto.UpdateRequest - getters and setters")
    void recipeUpdateRequest() {
        RecipeDto.UpdateRequest req = new RecipeDto.UpdateRequest();
        req.setTitle("Updated");
        req.setInstructions("New steps");
        req.setServings(4);
        req.setDescription("New desc");
        req.setCookingTimeMinutes(30);
        req.setCategoryId(2L);

        assertThat(req.getTitle()).isEqualTo("Updated");
        assertThat(req.getServings()).isEqualTo(4);
        assertThat(req.getCookingTimeMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("RecipeDto.Response - getters and setters")
    void recipeResponse() {
        RecipeDto.Response resp = new RecipeDto.Response();
        resp.setId(1L);
        resp.setTitle("Soup");
        resp.setAuthorUsername("chef");
        resp.setCategoryName("Soups");
        resp.setCreatedAt(LocalDateTime.now());
        resp.setUpdatedAt(LocalDateTime.now());
        resp.setDescription("desc");
        resp.setInstructions("steps");
        resp.setCookingTimeMinutes(30);
        resp.setServings(4);
        resp.setImageUrl("http://img.png");

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getTitle()).isEqualTo("Soup");
        assertThat(resp.getAuthorUsername()).isEqualTo("chef");
    }

    @Test
    @DisplayName("RecipeDto.Summary - getters and setters")
    void recipeSummary() {
        RecipeDto.Summary s = new RecipeDto.Summary();
        s.setId(1L);
        s.setTitle("Salad");
        s.setServings(1);
        s.setCategoryName("Salads");
        s.setAuthorUsername("user1");
        s.setDescription("fresh");
        s.setCookingTimeMinutes(10);

        assertThat(s.getTitle()).isEqualTo("Salad");
        assertThat(s.getCategoryName()).isEqualTo("Salads");
    }

    @Test
    @DisplayName("RecipeDto.RecipeIngredientDto - getters and setters")
    void recipeIngredientDto() {
        RecipeDto.RecipeIngredientDto dto = new RecipeDto.RecipeIngredientDto();
        dto.setIngredientId(1L);
        dto.setIngredientName("Tomato");
        dto.setUnit("g");
        dto.setQuantity(BigDecimal.valueOf(200));
        dto.setNotes("ripe");

        assertThat(dto.getIngredientName()).isEqualTo("Tomato");
        assertThat(dto.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    @DisplayName("RecipeDto.CreateRequest.IngredientItem - getters and setters")
    void recipeIngredientItem() {
        RecipeDto.CreateRequest.IngredientItem item = new RecipeDto.CreateRequest.IngredientItem();
        item.setIngredientId(5L);
        item.setQuantity(BigDecimal.valueOf(100));
        item.setNotes("chopped");

        assertThat(item.getIngredientId()).isEqualTo(5L);
        assertThat(item.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(item.getNotes()).isEqualTo("chopped");
    }
}
