package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.RecipeDto;
import com.example.recipeplanner.entity.*;
import com.example.recipeplanner.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecipeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("pass123"))
                .roles(Set.of(userRole))
                .build());
    }

    @Test
    @DisplayName("GET /api/recipes - public endpoint returns 200")
    void getRecipes_publicEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("POST /api/recipes - authenticated user can create")
    void createRecipe_authenticated_returns201() throws Exception {
        RecipeDto.CreateRequest request = new RecipeDto.CreateRequest();
        request.setTitle("Spaghetti Carbonara");
        request.setInstructions("Cook pasta. Mix eggs and cheese. Combine.");
        request.setServings(2);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spaghetti Carbonara"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/recipes - unauthenticated returns 401")
    void createRecipe_unauthenticated_returns401() throws Exception {
        RecipeDto.CreateRequest request = new RecipeDto.CreateRequest();
        request.setTitle("Test");
        request.setInstructions("Test");

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("GET /api/recipes/{id} - returns recipe details")
    void getRecipeById_returnsDetails() throws Exception {
        Recipe recipe = recipeRepository.save(Recipe.builder()
                .title("Test Recipe")
                .instructions("Do stuff")
                .author(testUser)
                .build());

        mockMvc.perform(get("/api/recipes/" + recipe.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recipe.getId()))
                .andExpect(jsonPath("$.title").value("Test Recipe"));
    }

    @Test
    @DisplayName("GET /api/recipes/{id} - not found returns 404")
    void getRecipeById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/recipes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("DELETE /api/recipes/{id} - owner can delete")
    void deleteRecipe_owner_returns204() throws Exception {
        Recipe recipe = recipeRepository.save(Recipe.builder()
                .title("To Delete")
                .instructions("Delete me")
                .author(testUser)
                .build());

        mockMvc.perform(delete("/api/recipes/" + recipe.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "otheruser", roles = "USER")
    @DisplayName("DELETE /api/recipes/{id} - non-owner returns 403")
    void deleteRecipe_nonOwner_returns403() throws Exception {
        Recipe recipe = recipeRepository.save(Recipe.builder()
                .title("Not Mine")
                .instructions("Do not delete")
                .author(testUser)
                .build());

        mockMvc.perform(delete("/api/recipes/" + recipe.getId()))
                .andExpect(status().isForbidden());
    }
}
