package com.example.recipeplanner.service.impl;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Ingredient;
import com.example.recipeplanner.entity.ShoppingList;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.exception.AccessDeniedException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.IngredientRepository;
import com.example.recipeplanner.repository.ShoppingListRepository;
import com.example.recipeplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    public DomainDtos.ShoppingListResponse addItem(DomainDtos.ShoppingListRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + request.getIngredientId()));

        ShoppingList item = ShoppingList.builder()
                .user(user)
                .ingredient(ingredient)
                .quantity(request.getQuantity())
                .build();

        return mapToResponse(shoppingListRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<DomainDtos.ShoppingListResponse> getMyList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return shoppingListRepository.findByUserId(user.getId()).stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public DomainDtos.ShoppingListResponse togglePurchased(Long id) {
        ShoppingList item = shoppingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!item.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your shopping list item");
        }
        item.setPurchased(!item.isPurchased());
        return mapToResponse(shoppingListRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        ShoppingList item = shoppingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!item.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your shopping list item");
        }
        shoppingListRepository.delete(item);
    }

    @Transactional
    public void clearPurchased() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        shoppingListRepository.deleteByUserIdAndPurchasedTrue(user.getId());
        log.info("Cleared purchased items for user '{}'", username);
    }

    private DomainDtos.ShoppingListResponse mapToResponse(ShoppingList s) {
        DomainDtos.ShoppingListResponse dto = new DomainDtos.ShoppingListResponse();
        dto.setId(s.getId());
        dto.setIngredientId(s.getIngredient().getId());
        dto.setIngredientName(s.getIngredient().getName());
        dto.setUnit(s.getIngredient().getUnit());
        dto.setQuantity(s.getQuantity());
        dto.setPurchased(s.isPurchased());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
