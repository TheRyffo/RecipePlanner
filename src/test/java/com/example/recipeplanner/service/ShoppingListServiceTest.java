package com.example.recipeplanner.service;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Ingredient;
import com.example.recipeplanner.entity.ShoppingList;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.exception.AccessDeniedException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.IngredientRepository;
import com.example.recipeplanner.repository.ShoppingListRepository;
import com.example.recipeplanner.repository.UserRepository;
import com.example.recipeplanner.service.impl.ShoppingListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock private ShoppingListRepository shoppingListRepository;
    @Mock private UserRepository userRepository;
    @Mock private IngredientRepository ingredientRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private User testUser;
    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("alice").email("a@b.com").password("x").build();
        testIngredient = Ingredient.builder().id(1L).name("Milk").unit("ml").build();

        var auth = new UsernamePasswordAuthenticationToken(
                "alice", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Add item - success")
    void addItem_success() {
        DomainDtos.ShoppingListRequest request = new DomainDtos.ShoppingListRequest();
        request.setIngredientId(1L);
        request.setQuantity(BigDecimal.valueOf(500));

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(shoppingListRepository.save(any())).thenAnswer(inv -> {
            ShoppingList s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        DomainDtos.ShoppingListResponse response = shoppingListService.addItem(request);

        assertThat(response.getIngredientName()).isEqualTo("Milk");
        assertThat(response.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Toggle purchased - flips status")
    void togglePurchased_flipsStatus() {
        ShoppingList item = ShoppingList.builder()
                .id(1L).user(testUser).ingredient(testIngredient)
                .quantity(BigDecimal.ONE).purchased(false).build();

        when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(item));
        when(shoppingListRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DomainDtos.ShoppingListResponse response = shoppingListService.togglePurchased(1L);

        assertThat(response.isPurchased()).isTrue();
    }

    @Test
    @DisplayName("Toggle purchased - wrong user throws AccessDenied")
    void togglePurchased_wrongUser_throwsAccessDenied() {
        User otherUser = User.builder().id(2L).username("bob").build();
        ShoppingList item = ShoppingList.builder()
                .id(1L).user(otherUser).ingredient(testIngredient)
                .quantity(BigDecimal.ONE).build();

        when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> shoppingListService.togglePurchased(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Delete item - not found throws ResourceNotFoundException")
    void deleteItem_notFound_throws() {
        when(shoppingListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.deleteItem(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get my list - returns list for current user")
    void getMyList_returnsUserItems() {
        ShoppingList item = ShoppingList.builder()
                .id(1L).user(testUser).ingredient(testIngredient)
                .quantity(BigDecimal.TEN).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(shoppingListRepository.findByUserId(1L)).thenReturn(List.of(item));

        var result = shoppingListService.getMyList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIngredientName()).isEqualTo("Milk");
    }
}
