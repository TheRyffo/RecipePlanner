package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Category;
import com.example.recipeplanner.entity.Role;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.repository.CategoryRepository;
import com.example.recipeplanner.repository.RoleRepository;
import com.example.recipeplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User adminUser;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        adminUser = userRepository.save(User.builder()
                .username("admin").email("admin@test.com")
                .password(passwordEncoder.encode("pass"))
                .roles(Set.of(adminRole, userRole)).build());
    }

    @Test
    @DisplayName("GET /api/categories - public returns 200")
    void getAll_public_returns200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/categories/{id} - found")
    void getById_found() throws Exception {
        Category cat = categoryRepository.save(
                Category.builder().name("Breakfast").createdBy(adminUser).build());
        mockMvc.perform(get("/api/categories/" + cat.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Breakfast"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - not found returns 404")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/categories/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("POST /api/categories - admin creates")
    void create_admin_returns201() throws Exception {
        DomainDtos.CategoryRequest req = new DomainDtos.CategoryRequest();
        req.setName("Dinner");
        req.setDescription("Evening meals");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dinner"));
    }

    @Test
    @DisplayName("POST /api/categories - unauthenticated returns 401")
    void create_unauthenticated_returns401() throws Exception {
        DomainDtos.CategoryRequest req = new DomainDtos.CategoryRequest();
        req.setName("Lunch");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("PUT /api/categories/{id} - update")
    void update_admin_returns200() throws Exception {
        Category cat = categoryRepository.save(
                Category.builder().name("Old Name").createdBy(adminUser).build());
        DomainDtos.CategoryRequest req = new DomainDtos.CategoryRequest();
        req.setName("New Name");

        mockMvc.perform(put("/api/categories/" + cat.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("DELETE /api/categories/{id} - admin deletes")
    void delete_admin_returns204() throws Exception {
        Category cat = categoryRepository.save(
                Category.builder().name("ToDelete").createdBy(adminUser).build());
        mockMvc.perform(delete("/api/categories/" + cat.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @DisplayName("POST /api/categories - duplicate returns 409")
    void create_duplicate_returns409() throws Exception {
        categoryRepository.save(Category.builder().name("Soups").createdBy(adminUser).build());
        DomainDtos.CategoryRequest req = new DomainDtos.CategoryRequest();
        req.setName("Soups");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
