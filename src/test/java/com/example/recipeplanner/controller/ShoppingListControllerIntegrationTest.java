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

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShoppingListControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private ShoppingListRepository shoppingListRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User testUser;
    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        shoppingListRepository.deleteAll();
        ingredientRepository.deleteAll();
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        testUser = userRepository.save(User.builder()
                .username("testuser").email("t@t.com")
                .password(passwordEncoder.encode("pass"))
                .roles(Set.of(userRole)).build());
        testIngredient = ingredientRepository.save(
                Ingredient.builder().name("Milk").unit("ml").build());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("POST /api/shopping-list - add item returns 201")
    void addItem_returns201() throws Exception {
        DomainDtos.ShoppingListRequest req = new DomainDtos.ShoppingListRequest();
        req.setIngredientId(testIngredient.getId());
        req.setQuantity(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/shopping-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ingredientName").value("Milk"))
                .andExpect(jsonPath("$.purchased").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("GET /api/shopping-list - returns user list")
    void getMyList_returns200() throws Exception {
        mockMvc.perform(get("/api/shopping-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/shopping-list - unauthenticated returns 401")
    void getMyList_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/shopping-list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("PATCH /api/shopping-list/{id}/toggle - toggles purchased")
    void toggle_returns200() throws Exception {
        ShoppingList item = shoppingListRepository.save(ShoppingList.builder()
                .user(testUser).ingredient(testIngredient)
                .quantity(BigDecimal.ONE).purchased(false).build());

        mockMvc.perform(patch("/api/shopping-list/" + item.getId() + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchased").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("DELETE /api/shopping-list/{id} - owner deletes")
    void deleteItem_owner_returns204() throws Exception {
        ShoppingList item = shoppingListRepository.save(ShoppingList.builder()
                .user(testUser).ingredient(testIngredient)
                .quantity(BigDecimal.TEN).build());

        mockMvc.perform(delete("/api/shopping-list/" + item.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("DELETE /api/shopping-list/purchased - clears purchased")
    void clearPurchased_returns204() throws Exception {
        mockMvc.perform(delete("/api/shopping-list/purchased"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "otheruser", roles = "USER")
    @DisplayName("PATCH toggle - other user returns 403")
    void toggle_otherUser_returns403() throws Exception {
        ShoppingList item = shoppingListRepository.save(ShoppingList.builder()
                .user(testUser).ingredient(testIngredient)
                .quantity(BigDecimal.ONE).build());

        mockMvc.perform(patch("/api/shopping-list/" + item.getId() + "/toggle"))
                .andExpect(status().isForbidden());
    }
}
