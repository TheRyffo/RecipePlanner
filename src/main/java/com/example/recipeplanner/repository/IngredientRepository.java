package com.example.recipeplanner.repository;

import com.example.recipeplanner.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);
    boolean existsByName(String name);
    Page<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
