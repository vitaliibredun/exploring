package ru.practicum.ewm.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto categoryDto);

    CategoryDto updateCategory(Long catId, NewCategoryDto categoryDto);

    void deleteCategory(Long catId);

    List<CategoryDto> getAllCategories(Pageable pageable);

    CategoryDto getCategory(Long catId);
}