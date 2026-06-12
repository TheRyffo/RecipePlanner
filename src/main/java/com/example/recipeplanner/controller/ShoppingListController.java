package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.service.impl.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
@Tag(name = "Shopping List", description = "Manage your grocery shopping list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @PostMapping
    @Operation(summary = "Add item to shopping list", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DomainDtos.ShoppingListResponse> addItem(
            @Valid @RequestBody DomainDtos.ShoppingListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.addItem(request));
    }

    @GetMapping
    @Operation(summary = "Get my shopping list", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<DomainDtos.ShoppingListResponse>> getMyList() {
        return ResponseEntity.ok(shoppingListService.getMyList());
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle purchased status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DomainDtos.ShoppingListResponse> togglePurchased(@PathVariable Long id) {
        return ResponseEntity.ok(shoppingListService.togglePurchased(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove item from shopping list", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        shoppingListService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/purchased")
    @Operation(summary = "Clear all purchased items", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> clearPurchased() {
        shoppingListService.clearPurchased();
        return ResponseEntity.noContent().build();
    }
}
