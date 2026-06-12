package com.example.recipeplanner.service.impl;

import com.example.recipeplanner.dto.RecipeDto;
import com.example.recipeplanner.entity.*;
import com.example.recipeplanner.exception.AccessDeniedException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    public RecipeDto.Response create(RecipeDto.CreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        }

        Recipe recipe = Recipe.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .cookingTimeMinutes(request.getCookingTimeMinutes())
                .servings(request.getServings())
                .difficulty(request.getDifficulty())
                .imageUrl(request.getImageUrl())
                .author(author)
                .category(category)
                .build();

        if (request.getIngredients() != null) {
            for (var item : request.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + item.getIngredientId()));
                RecipeIngredient ri = RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .quantity(item.getQuantity())
                        .notes(item.getNotes())
                        .build();
                recipe.getRecipeIngredients().add(ri);
            }
        }

        Recipe saved = recipeRepository.save(recipe);
        log.info("Created recipe '{}' by user '{}'", saved.getTitle(), username);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public RecipeDto.Response getById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + id));
        return mapToResponse(recipe);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDto.Summary> search(String title, Long categoryId, Recipe.Difficulty difficulty, Pageable pageable) {
        return recipeRepository.findByFilters(title, categoryId, difficulty, pageable)
                .map(this::mapToSummary);
    }

    @Transactional
    public RecipeDto.Response update(Long id, RecipeDto.UpdateRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!recipe.getAuthor().getUsername().equals(username) && !isAdmin) {
            throw new AccessDeniedException("You can only edit your own recipes");
        }

        if (request.getTitle() != null) recipe.setTitle(request.getTitle());
        if (request.getDescription() != null) recipe.setDescription(request.getDescription());
        if (request.getInstructions() != null) recipe.setInstructions(request.getInstructions());
        if (request.getCookingTimeMinutes() != null) recipe.setCookingTimeMinutes(request.getCookingTimeMinutes());
        if (request.getServings() != null) recipe.setServings(request.getServings());
        if (request.getDifficulty() != null) recipe.setDifficulty(request.getDifficulty());
        if (request.getImageUrl() != null) recipe.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            recipe.setCategory(category);
        }

        if (request.getIngredients() != null) {
            recipe.getRecipeIngredients().clear();
            for (var item : request.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + item.getIngredientId()));
                RecipeIngredient ri = RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .quantity(item.getQuantity())
                        .notes(item.getNotes())
                        .build();
                recipe.getRecipeIngredients().add(ri);
            }
        }

        return mapToResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!recipe.getAuthor().getUsername().equals(username) && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own recipes");
        }

        recipeRepository.delete(recipe);
        log.info("Deleted recipe id={} by user '{}'", id, username);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDto.Summary> getMyRecipes(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return recipeRepository.findByAuthorId(user.getId(), pageable).map(this::mapToSummary);
    }

    private RecipeDto.Response mapToResponse(Recipe recipe) {
        RecipeDto.Response dto = new RecipeDto.Response();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setInstructions(recipe.getInstructions());
        dto.setCookingTimeMinutes(recipe.getCookingTimeMinutes());
        dto.setServings(recipe.getServings());
        dto.setDifficulty(recipe.getDifficulty());
        dto.setImageUrl(recipe.getImageUrl());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());
        dto.setAuthorUsername(recipe.getAuthor().getUsername());
        dto.setCategoryName(recipe.getCategory() != null ? recipe.getCategory().getName() : null);

        List<RecipeDto.RecipeIngredientDto> ingredients = recipe.getRecipeIngredients().stream()
                .map(ri -> {
                    RecipeDto.RecipeIngredientDto riDto = new RecipeDto.RecipeIngredientDto();
                    riDto.setIngredientId(ri.getIngredient().getId());
                    riDto.setIngredientName(ri.getIngredient().getName());
                    riDto.setUnit(ri.getIngredient().getUnit());
                    riDto.setQuantity(ri.getQuantity());
                    riDto.setNotes(ri.getNotes());
                    return riDto;
                }).toList();
        dto.setIngredients(ingredients);
        return dto;
    }

    private RecipeDto.Summary mapToSummary(Recipe recipe) {
        RecipeDto.Summary summary = new RecipeDto.Summary();
        summary.setId(recipe.getId());
        summary.setTitle(recipe.getTitle());
        summary.setDescription(recipe.getDescription());
        summary.setCookingTimeMinutes(recipe.getCookingTimeMinutes());
        summary.setServings(recipe.getServings());
        summary.setDifficulty(recipe.getDifficulty());
        summary.setCategoryName(recipe.getCategory() != null ? recipe.getCategory().getName() : null);
        summary.setAuthorUsername(recipe.getAuthor().getUsername());
        return summary;
    }
}
