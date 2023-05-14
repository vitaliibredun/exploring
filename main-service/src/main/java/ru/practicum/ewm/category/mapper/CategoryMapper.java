package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

@Mapper
public interface CategoryMapper {
    default Category toModel(NewCategoryDto categoryDto) {
        if (categoryDto == null) {
            return null;
        }

        Category.CategoryBuilder builder = Category.builder();

        builder.name(categoryDto.getName());

        return builder.build();
    }

    default CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto.CategoryDtoBuilder builder = CategoryDto.builder();

        builder.id(category.getId());
        builder.name(category.getName());

        return builder.build();
    }
}