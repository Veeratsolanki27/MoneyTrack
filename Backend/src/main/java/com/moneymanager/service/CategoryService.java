package com.moneymanager.service;

import com.moneymanager.dto.TransactionDtos.CategoryRequest;
import com.moneymanager.dto.TransactionDtos.CategoryResponse;
import com.moneymanager.entity.Category;
import com.moneymanager.entity.User;
import com.moneymanager.repository.CategoryRepository;
import com.moneymanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryResponse> getCategories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return categoryRepository.findByUserIdOrUserIsNullOrderByIdAsc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(String email, CategoryRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon() != null ? request.getIcon() : "🏷️")
                .colorHex(request.getColorHex() != null ? request.getColorHex() : "#10b981")
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Category not found or cannot delete default"));

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category c) {
        CategoryResponse res = new CategoryResponse();
        res.setId(c.getId());
        res.setName(c.getName());
        res.setType(c.getType());
        res.setIcon(c.getIcon());
        res.setColorHex(c.getColorHex());
        return res;
    }
}

