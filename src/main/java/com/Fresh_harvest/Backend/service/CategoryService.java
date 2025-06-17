package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.dto.CategoryRequestDto;
import com.Fresh_harvest.Backend.model.Category;
import com.Fresh_harvest.Backend.exception.ResourceNotFoundException;
import com.Fresh_harvest.Backend.exception.ValidationException;
import com.Fresh_harvest.Backend.repository.CategoryRepository;
import com.Fresh_harvest.Backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // --- CRUD Operations ---

    @Transactional
    public Category createCategory(CategoryRequestDto requestDto) {
        if (categoryRepository.findByName(requestDto.getName()).isPresent()) {
            throw new ValidationException("Category with name '" + requestDto.getName() + "' already exists.");
        }
        Category category = new Category();
        category.setName(requestDto.getName());
        category.setDescription(requestDto.getDescription());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequestDto requestDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (!existingCategory.getName().equalsIgnoreCase(requestDto.getName())) {
            if (categoryRepository.findByName(requestDto.getName()).isPresent()) {
                throw new ValidationException("Category with name '" + requestDto.getName() + "' already exists.");
            }
        }
        existingCategory.setName(requestDto.getName());
        existingCategory.setDescription(requestDto.getDescription());
        return categoryRepository.save(existingCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category categoryToDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        productRepository.disassociateProductsAndMarkUnavailableByCategoryId(categoryToDelete.getId());

        categoryRepository.delete(categoryToDelete);
    }
}