package com.example.recipeplanner.service;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Ingredient;
import com.example.recipeplanner.exception.DuplicateResourceException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.IngredientRepository;
import com.example.recipeplanner.service.impl.IngredientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    private Ingredient buildIngredient(Long id, String name) {
        return Ingredient.builder()
                .id(id)
                .name(name)
                .unit("g")
                .caloriesPerUnit(BigDecimal.valueOf(1.5))
                .build();
    }

    @Test
    @DisplayName("Create ingredient - success")
    void create_success() {
        DomainDtos.IngredientRequest request = new DomainDtos.IngredientRequest();
        request.setName("Flour");
        request.setUnit("g");
        request.setCaloriesPerUnit(BigDecimal.valueOf(3.64));

        when(ingredientRepository.existsByName("Flour")).thenReturn(false);
        when(ingredientRepository.save(any())).thenAnswer(inv -> {
            Ingredient i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        DomainDtos.IngredientResponse response = ingredientService.create(request);

        assertThat(response.getName()).isEqualTo("Flour");
        assertThat(response.getUnit()).isEqualTo("g");
    }

    @Test
    @DisplayName("Create ingredient - duplicate name throws exception")
    void create_duplicateName_throwsDuplicate() {
        DomainDtos.IngredientRequest request = new DomainDtos.IngredientRequest();
        request.setName("Sugar");
        request.setUnit("g");

        when(ingredientRepository.existsByName("Sugar")).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Sugar");
    }

    @Test
    @DisplayName("Get by ID - found")
    void getById_found() {
        Ingredient ingredient = buildIngredient(1L, "Salt");
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        DomainDtos.IngredientResponse response = ingredientService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Salt");
    }

    @Test
    @DisplayName("Get by ID - not found throws exception")
    void getById_notFound_throwsException() {
        when(ingredientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get all without search returns all paged")
    void getAll_noSearch_returnsAll() {
        Ingredient i1 = buildIngredient(1L, "Salt");
        Ingredient i2 = buildIngredient(2L, "Pepper");
        Page<Ingredient> page = new PageImpl<>(List.of(i1, i2));

        when(ingredientRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<DomainDtos.IngredientResponse> result = ingredientService.getAll(null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Delete - not found throws exception")
    void delete_notFound_throwsException() {
        when(ingredientRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> ingredientService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ingredientRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Delete - success")
    void delete_success() {
        when(ingredientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ingredientRepository).deleteById(1L);

        ingredientService.delete(1L);

        verify(ingredientRepository).deleteById(1L);
    }
}
