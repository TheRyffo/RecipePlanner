package com.example.recipeplanner.repository;

import com.example.recipeplanner.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    List<ShoppingList> findByUserId(Long userId);

    List<ShoppingList> findByUserIdAndPurchased(Long userId, boolean purchased);

    void deleteByUserIdAndPurchasedTrue(Long userId);

    boolean existsByUserIdAndIngredientId(Long userId, Long ingredientId);
}
