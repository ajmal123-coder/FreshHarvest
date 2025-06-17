package com.Fresh_harvest.Backend.controller;

import com.Fresh_harvest.Backend.dto.CategoryRequestDto;
import com.Fresh_harvest.Backend.dto.CategoryResponseDto;
import com.Fresh_harvest.Backend.model.Category;
import com.Fresh_harvest.Backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Categories", description = "Category management APIs for creating, retrieving, updating, and deleting product categories.")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // --- Helper Method for DTO Conversion ---
    private CategoryResponseDto convertToCategoryResponseDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    // --- Public Read Endpoints (Accessible to all) ---

    @Operation(summary = "Get category by ID",
            description = "Retrieves a single category by its unique ID. Accessible to all users.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(convertToCategoryResponseDto(category));
    }

    @Operation(summary = "Get all categories",
            description = "Retrieves a paginated list of all product categories. Accessible to all users.")
    @GetMapping
    public ResponseEntity<Page<CategoryResponseDto>> getAllCategories(Pageable pageable) {
        Page<Category> categoriesPage = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categoriesPage.map(this::convertToCategoryResponseDto));
    }

    // --- Admin-Only Endpoints (Requires ADMIN Role) ---

    @Operation(summary = "Create a new category",
            description = "Creates a new product category with a unique name. Requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
        Category newCategory = categoryService.createCategory(requestDto);
        return new ResponseEntity<>(convertToCategoryResponseDto(newCategory), HttpStatus.CREATED); // 201 Created
    }

    @Operation(summary = "Update an existing category",
            description = "Updates the name of an existing category by ID. Requires ADMIN role.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody CategoryRequestDto requestDto) {
        Category updatedCategory = categoryService.updateCategory(id, requestDto);
        return ResponseEntity.ok(convertToCategoryResponseDto(updatedCategory)); // 200 OK
    }

    @Operation(summary = "Delete a category",
            description = "Deletes a category by its ID. Products associated with this category will be disassociated and marked as unavailable. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}