package com.example.recipeplanner.service.impl;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Ingredient;
import com.example.recipeplanner.exception.DuplicateResourceException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional
    public DomainDtos.IngredientResponse create(DomainDtos.IngredientRequest request) {
        if (ingredientRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Ingredient already exists: " + request.getName());
        }
        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .unit(request.getUnit())
                .caloriesPerUnit(request.getCaloriesPerUnit())
                .build();
        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Transactional(readOnly = true)
    public DomainDtos.IngredientResponse getById(Long id) {
        return mapToResponse(ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<DomainDtos.IngredientResponse> getAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ingredientRepository.findByNameContainingIgnoreCase(search, pageable).map(this::mapToResponse);
        }
        return ingredientRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public DomainDtos.IngredientResponse update(Long id, DomainDtos.IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + id));

        if (!ingredient.getName().equals(request.getName()) && ingredientRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Ingredient already exists: " + request.getName());
        }

        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setCaloriesPerUnit(request.getCaloriesPerUnit());
        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void delete(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingredient not found: " + id);
        }
        ingredientRepository.deleteById(id);
        log.info("Deleted ingredient id={}", id);
    }

    private DomainDtos.IngredientResponse mapToResponse(Ingredient i) {
        DomainDtos.IngredientResponse dto = new DomainDtos.IngredientResponse();
        dto.setId(i.getId());
        dto.setName(i.getName());
        dto.setUnit(i.getUnit());
        dto.setCaloriesPerUnit(i.getCaloriesPerUnit());
        return dto;
    }
}
