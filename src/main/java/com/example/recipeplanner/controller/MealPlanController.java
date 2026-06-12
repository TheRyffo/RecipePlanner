package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.service.impl.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
@Tag(name = "Meal Plans", description = "Plan your meals for the week")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping
    @Operation(summary = "Add a meal to your plan", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DomainDtos.MealPlanResponse> create(@Valid @RequestBody DomainDtos.MealPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealPlanService.create(request));
    }

    @GetMapping("/week")
    @Operation(summary = "Get meal plan for a date range", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<DomainDtos.MealPlanResponse>> getWeekPlan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(mealPlanService.getWeekPlan(from, to));
    }

    @GetMapping("/day")
    @Operation(summary = "Get meal plan for a specific day", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<DomainDtos.MealPlanResponse>> getDayPlan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(mealPlanService.getDayPlan(targetDate));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a meal plan entry", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DomainDtos.MealPlanResponse> update(@PathVariable Long id,
                                                               @Valid @RequestBody DomainDtos.MealPlanRequest request) {
        return ResponseEntity.ok(mealPlanService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a meal from plan", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
