package com.example.recipeplanner.repository;

import com.example.recipeplanner.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Recipe> findByAuthorId(Long authorId, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Исправленный запрос — явные касты чтобы PostgreSQL не путал типы
    @Query("SELECT r FROM Recipe r WHERE " +
           "(:title IS NULL OR LOWER(CAST(r.title AS string)) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) AND " +
           "(:categoryId IS NULL OR r.category.id = :categoryId) AND " +
           "(:difficulty IS NULL OR r.difficulty = :difficulty)")
    Page<Recipe> findByFilters(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("difficulty") Recipe.Difficulty difficulty,
            Pageable pageable
    );

    List<Recipe> findByAuthorUsername(String username);
}
