package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.RecipeDto;
import com.example.recipeplanner.entity.Recipe;
import com.example.recipeplanner.service.impl.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "Recipe management")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    @Operation(summary = "Create a new recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RecipeDto.Response> create(@Valid @RequestBody RecipeDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by ID")
    public ResponseEntity<RecipeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search recipes with filters and pagination")
    public ResponseEntity<Page<RecipeDto.Summary>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Recipe.Difficulty difficulty,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(recipeService.search(title, categoryId, difficulty, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RecipeDto.Response> update(@PathVariable Long id,
                                                      @Valid @RequestBody RecipeDto.UpdateRequest request) {
        return ResponseEntity.ok(recipeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's recipes", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<RecipeDto.Summary>> getMyRecipes(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(recipeService.getMyRecipes(pageable));
    }
}
