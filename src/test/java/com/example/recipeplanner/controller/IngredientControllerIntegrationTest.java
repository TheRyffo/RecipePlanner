package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Ingredient;
import com.example.recipeplanner.entity.Role;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.repository.IngredientRepository;
import com.example.recipeplanner.repository.RoleRepository;
import com.example.recipeplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IngredientControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        userRepository.deleteAll();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        userRepository.save(User.builder()
                .username("admin").email("admin@test.com")
                .password(passwordEncoder.encode("pass"))
                .roles(Set.of(adminRole, userRole)).build());
    }

    @Test
    @DisplayName("GET /api/ingredients - returns 200 paged")
    void getAll_returns200() throws Exception {
        ingredientRepository.save(Ingredient.builder().name("Salt").unit("g").build());
        mockMvc.perform(get("/api/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/ingredients/{id} - found returns 200")
    void getById_found_returns200() throws Exception {
        Ingredient saved = ingredientRepository.save(
                Ingredient.builder().name("Sugar").unit("g").build());
        mockMvc.perform(get("/api/ingredients/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sugar"));
    }

    @Test
    @DisplayName("GET /api/ingredients/{id} - not found returns 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/ingredients/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("POST /api/ingredients - admin creates ingredient")
    void create_admin_returns201() throws Exception {
        DomainDtos.IngredientRequest req = new DomainDtos.IngredientRequest();
        req.setName("Flour");
        req.setUnit("g");
        req.setCaloriesPerUnit(BigDecimal.valueOf(3.64));

        mockMvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Flour"));
    }

    @Test
    @DisplayName("POST /api/ingredients - unauthenticated returns 401")
    void create_unauthenticated_returns401() throws Exception {
        DomainDtos.IngredientRequest req = new DomainDtos.IngredientRequest();
        req.setName("Oil");
        req.setUnit("ml");

        mockMvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("PUT /api/ingredients/{id} - update")
    void update_admin_returns200() throws Exception {
        Ingredient saved = ingredientRepository.save(
                Ingredient.builder().name("Pepper").unit("g").build());

        DomainDtos.IngredientRequest req = new DomainDtos.IngredientRequest();
        req.setName("Black Pepper");
        req.setUnit("g");

        mockMvc.perform(put("/api/ingredients/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Black Pepper"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("DELETE /api/ingredients/{id} - admin deletes")
    void delete_admin_returns204() throws Exception {
        Ingredient saved = ingredientRepository.save(
                Ingredient.builder().name("Basil").unit("g").build());
        mockMvc.perform(delete("/api/ingredients/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("POST /api/ingredients - duplicate name returns 409")
    void create_duplicate_returns409() throws Exception {
        ingredientRepository.save(Ingredient.builder().name("Salt").unit("g").build());

        DomainDtos.IngredientRequest req = new DomainDtos.IngredientRequest();
        req.setName("Salt");
        req.setUnit("g");

        mockMvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
