package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.*;
import com.example.recipeplanner.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MealPlanControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MealPlanRepository mealPlanRepository;
    @Autowired private RecipeRepository recipeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User testUser;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        mealPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        testUser = userRepository.save(User.builder()
                .username("testuser").email("test@test.com")
                .password(passwordEncoder.encode("pass"))
                .roles(Set.of(userRole)).build());
        testRecipe = recipeRepository.save(Recipe.builder()
                .title("Test Recipe").instructions("Do stuff")
                .author(testUser).build());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("POST /api/meal-plans - create returns 201")
    void create_returns201() throws Exception {
        DomainDtos.MealPlanRequest req = new DomainDtos.MealPlanRequest();
        req.setRecipeId(testRecipe.getId());
        req.setPlannedDate(LocalDate.now());
        req.setMealType(MealPlan.MealType.LUNCH);
        req.setServings(2);

        mockMvc.perform(post("/api/meal-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.recipeTitle").value("Test Recipe"));
    }

    @Test
    @DisplayName("POST /api/meal-plans - unauthenticated returns 401")
    void create_unauthenticated_returns401() throws Exception {
        DomainDtos.MealPlanRequest req = new DomainDtos.MealPlanRequest();
        req.setRecipeId(testRecipe.getId());
        req.setPlannedDate(LocalDate.now());
        req.setMealType(MealPlan.MealType.DINNER);

        mockMvc.perform(post("/api/meal-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("GET /api/meal-plans/week - returns list")
    void getWeekPlan_returns200() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/meal-plans/week")
                        .param("from", today.toString())
                        .param("to", today.plusDays(6).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("GET /api/meal-plans/day - returns list")
    void getDayPlan_returns200() throws Exception {
        mockMvc.perform(get("/api/meal-plans/day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("DELETE /api/meal-plans/{id} - owner deletes")
    void delete_owner_returns204() throws Exception {
        MealPlan mp = mealPlanRepository.save(MealPlan.builder()
                .user(testUser).recipe(testRecipe)
                .plannedDate(LocalDate.now())
                .mealType(MealPlan.MealType.BREAKFAST)
                .servings(1).build());

        mockMvc.perform(delete("/api/meal-plans/" + mp.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("DELETE /api/meal-plans/{id} - not found returns 404")
    void delete_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/meal-plans/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("PUT /api/meal-plans/{id} - update")
    void update_returns200() throws Exception {
        MealPlan mp = mealPlanRepository.save(MealPlan.builder()
                .user(testUser).recipe(testRecipe)
                .plannedDate(LocalDate.now())
                .mealType(MealPlan.MealType.BREAKFAST)
                .servings(1).build());

        DomainDtos.MealPlanRequest req = new DomainDtos.MealPlanRequest();
        req.setRecipeId(testRecipe.getId());
        req.setPlannedDate(LocalDate.now().plusDays(1));
        req.setMealType(MealPlan.MealType.DINNER);
        req.setServings(3);

        mockMvc.perform(put("/api/meal-plans/" + mp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealType").value("DINNER"));
    }
}
