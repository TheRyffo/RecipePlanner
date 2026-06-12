package com.example.recipeplanner.service.impl;

import com.example.recipeplanner.dto.DomainDtos;
import com.example.recipeplanner.entity.Category;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.exception.DuplicateResourceException;
import com.example.recipeplanner.exception.ResourceNotFoundException;
import com.example.recipeplanner.repository.CategoryRepository;
import com.example.recipeplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public DomainDtos.CategoryResponse create(DomainDtos.CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(user)
                .build();
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public DomainDtos.CategoryResponse getById(Long id) {
        return mapToResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<DomainDtos.CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public DomainDtos.CategoryResponse update(Long id, DomainDtos.CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private DomainDtos.CategoryResponse mapToResponse(Category c) {
        DomainDtos.CategoryResponse dto = new DomainDtos.CategoryResponse();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setCreatedByUsername(c.getCreatedBy().getUsername());
        return dto;
    }
}
