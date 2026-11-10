package com.moneymanager.controller;

import com.moneymanager.dto.TransactionDtos.CategoryRequest;
import com.moneymanager.dto.TransactionDtos.CategoryResponse;
import com.moneymanager.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(categoryService.getCategories(email));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal String email,
                                                           @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(email, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id,
                                               @AuthenticationPrincipal String email) {
        categoryService.deleteCategory(id, email);
        return ResponseEntity.noContent().build();
    }
}

