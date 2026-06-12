package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.service.impl.IngredientService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Tag(name = "Ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create ingredient (admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DomainDtos.IngredientResponse> create(@Valid @RequestBody DomainDtos.IngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainDtos.IngredientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DomainDtos.IngredientResponse>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ingredientService.getAll(search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update ingredient (admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DomainDtos.IngredientResponse> update(@PathVariable Long id,
                                                                 @Valid @RequestBody DomainDtos.IngredientRequest request) {
        return ResponseEntity.ok(ingredientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete ingredient (admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
