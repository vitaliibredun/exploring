package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto categoryDto) {
        try {
            Category category = mapper.toModel(categoryDto);
            Category categoryFromRepository = repository.save(category);
            return mapper.toDto(categoryFromRepository);
        } catch (DataIntegrityViolationException exception) {
            log.error("Field: name. Error: the same name");
            throw new ConflictException("The field of name must be unique");
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, NewCategoryDto categoryDto) {
        try {
            Category newCategory = mapper.toModel(categoryDto);
            Category oldCategory = checkCategoryExist(catId);
            oldCategory.setName(newCategory.getName());
            Category updatedCategory = repository.saveAndFlush(oldCategory);
            return mapper.toDto(updatedCategory);
        } catch (DataIntegrityViolationException exception) {
            log.error("Field: name. Error: the same name");
            throw new ConflictException("The field of name must be unique");
        }
    }

    @Override
    public void deleteCategory(Long catId) {
        try {
            repository.deleteById(catId);
        } catch (DataIntegrityViolationException exception) {
            log.error("Error: there is depending on event");
            throw new ConflictException("There is depending on event");
        }
    }

    @Override
    public List<CategoryDto> getAllCategories(Pageable pageable) {
        return repository.findAll(pageable)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = checkCategoryExist(catId);
        return mapper.toDto(category);
    }

    private Category checkCategoryExist(Long catId) {
        Optional<Category> category = repository.findById(catId);
        if (category.isEmpty()) {
            log.error("Validation failed. The category with id {} doesn't exist", catId);
            throw new NotExistsException("The required object was not found.");
        }
        return category.get();
    }
}
