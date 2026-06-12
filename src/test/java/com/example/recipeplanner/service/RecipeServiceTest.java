package com.example.recipeplanner.service;

import com.example.recipeplanner.dto.RecipeDto;
import com.example.recipeplanner.entity.*;
import com.example.recipeplanner.exception.AccessDeniedException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.CategoryRepository;
import com.example.recipeplanner.repository.IngredientRepository;
import com.example.recipeplanner.repository.RecipeRepository;
import com.example.recipeplanner.repository.UserRepository;
import com.example.recipeplanner.service.impl.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    private User testUser;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .build();

        testRecipe = Recipe.builder()
                .id(1L)
                .title("Test Recipe")
                .instructions("Mix everything")
                .author(testUser)
                .build();

        setAuthentication("testuser", "ROLE_USER");
    }

    private void setAuthentication(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Create recipe - success")
    void createRecipe_success() {
        RecipeDto.CreateRequest request = new RecipeDto.CreateRequest();
        request.setTitle("New Recipe");
        request.setInstructions("Cook it");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });

        RecipeDto.Response response = recipeService.create(request);

        assertThat(response.getTitle()).isEqualTo("New Recipe");
        assertThat(response.getAuthorUsername()).isEqualTo("testuser");
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Get recipe by ID - found")
    void getById_found() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        RecipeDto.Response response = recipeService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Recipe");
    }

    @Test
    @DisplayName("Get recipe by ID - not found throws exception")
    void getById_notFound_throwsException() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Delete recipe - owner can delete")
    void delete_ownerCanDelete() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        doNothing().when(recipeRepository).delete(testRecipe);

        recipeService.delete(1L);

        verify(recipeRepository).delete(testRecipe);
    }

    @Test
    @DisplayName("Delete recipe - other user cannot delete")
    void delete_otherUser_throwsAccessDenied() {
        setAuthentication("otheruser", "ROLE_USER");
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        assertThatThrownBy(() -> recipeService.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Delete recipe - admin can delete any recipe")
    void delete_adminCanDeleteAny() {
        setAuthentication("admin", "ROLE_ADMIN");
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        doNothing().when(recipeRepository).delete(testRecipe);

        recipeService.delete(1L);

        verify(recipeRepository).delete(testRecipe);
    }

    @Test
    @DisplayName("Search recipes returns paged results")
    void search_returnsPaged() {
        Page<Recipe> page = new PageImpl<>(List.of(testRecipe));
        when(recipeRepository.findByFilters(null, null, null, Pageable.unpaged()))
                .thenReturn(page);

        Page<RecipeDto.Summary> result = recipeService.search(null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Recipe");
    }
}
