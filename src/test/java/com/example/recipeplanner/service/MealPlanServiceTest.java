package com.example.recipeplanner.service;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.*;
import com.example.recipeplanner.exception.AccessDeniedException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.MealPlanRepository;
import com.example.recipeplanner.repository.RecipeRepository;
import com.example.recipeplanner.repository.UserRepository;
import com.example.recipeplanner.service.impl.MealPlanService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock private MealPlanRepository mealPlanRepository;
    @Mock private UserRepository userRepository;
    @Mock private RecipeRepository recipeRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    private User testUser;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("alice").email("a@b.com").password("x").build();
        testRecipe = Recipe.builder().id(1L).title("Salad").instructions("Mix").author(testUser).build();

        var auth = new UsernamePasswordAuthenticationToken(
                "alice", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Create meal plan - success")
    void create_success() {
        DomainDtos.MealPlanRequest request = new DomainDtos.MealPlanRequest();
        request.setRecipeId(1L);
        request.setPlannedDate(LocalDate.now());
        request.setMealType(MealPlan.MealType.LUNCH);
        request.setServings(2);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(mealPlanRepository.save(any())).thenAnswer(inv -> {
            MealPlan mp = inv.getArgument(0);
            mp.setId(1L);
            return mp;
        });

        DomainDtos.MealPlanResponse response = mealPlanService.create(request);

        assertThat(response.getRecipeTitle()).isEqualTo("Salad");
        assertThat(response.getMealType()).isEqualTo(MealPlan.MealType.LUNCH);
    }

    @Test
    @DisplayName("Create meal plan - recipe not found throws exception")
    void create_recipeNotFound_throwsException() {
        DomainDtos.MealPlanRequest request = new DomainDtos.MealPlanRequest();
        request.setRecipeId(99L);
        request.setPlannedDate(LocalDate.now());
        request.setMealType(MealPlan.MealType.DINNER);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Delete meal plan - owner can delete")
    void delete_owner_success() {
        MealPlan mp = MealPlan.builder().id(1L).user(testUser).recipe(testRecipe)
                .plannedDate(LocalDate.now()).mealType(MealPlan.MealType.BREAKFAST).build();

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mp));
        doNothing().when(mealPlanRepository).delete(mp);

        mealPlanService.delete(1L);

        verify(mealPlanRepository).delete(mp);
    }

    @Test
    @DisplayName("Delete meal plan - other user throws AccessDenied")
    void delete_wrongUser_throwsAccessDenied() {
        User otherUser = User.builder().id(2L).username("bob").build();
        MealPlan mp = MealPlan.builder().id(1L).user(otherUser).recipe(testRecipe)
                .plannedDate(LocalDate.now()).mealType(MealPlan.MealType.SNACK).build();

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mp));

        assertThatThrownBy(() -> mealPlanService.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Get week plan - returns meals in range")
    void getWeekPlan_returnsMeals() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(6);

        MealPlan mp = MealPlan.builder().id(1L).user(testUser).recipe(testRecipe)
                .plannedDate(from).mealType(MealPlan.MealType.LUNCH).servings(1).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(mealPlanRepository.findByUserIdAndPlannedDateBetween(1L, from, to))
                .thenReturn(List.of(mp));

        List<DomainDtos.MealPlanResponse> result = mealPlanService.getWeekPlan(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipeTitle()).isEqualTo("Salad");
    }
}
