package com.example.recipeplanner.service;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Category;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.exception.DuplicateResourceException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.CategoryRepository;
import com.example.recipeplanner.repository.UserRepository;
import com.example.recipeplanner.service.impl.CategoryService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).username("admin").email("admin@test.com").password("x").build();
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Create category - success")
    void create_success() {
        DomainDtos.CategoryRequest request = new DomainDtos.CategoryRequest();
        request.setName("Breakfast");
        request.setDescription("Morning meals");

        when(categoryRepository.existsByName("Breakfast")).thenReturn(false);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        DomainDtos.CategoryResponse response = categoryService.create(request);

        assertThat(response.getName()).isEqualTo("Breakfast");
        assertThat(response.getCreatedByUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Create category - duplicate name throws exception")
    void create_duplicate_throwsException() {
        DomainDtos.CategoryRequest request = new DomainDtos.CategoryRequest();
        request.setName("Lunch");

        when(categoryRepository.existsByName("Lunch")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Get all - returns list")
    void getAll_returnsList() {
        Category c1 = Category.builder().id(1L).name("Breakfast").createdBy(adminUser).build();
        Category c2 = Category.builder().id(2L).name("Dinner").createdBy(adminUser).build();

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<DomainDtos.CategoryResponse> result = categoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Breakfast", "Dinner");
    }

    @Test
    @DisplayName("Delete - not found throws exception")
    void delete_notFound_throwsException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
