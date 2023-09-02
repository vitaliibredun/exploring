package ru.practicum.ewm.category;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.exception.NotExistsException;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
public class CategoryServiceImplTest {
    private final CategoryService service;
    private final CategoryRepository repository;
    private final EntityManager entityManager;
    private NewCategoryDto newCategoryDto1;
    private NewCategoryDto newCategoryDto2;
    private NewCategoryDto newCategoryDto3;
    private NewCategoryDto newCategoryDto4;
    private NewCategoryDto newCategoryDto5;

    @BeforeEach
    void setUp() {
        resetIdColumns();

        newCategoryDto1 = Instancio.create(NewCategoryDto.class);
        newCategoryDto2 = Instancio.create(NewCategoryDto.class);
        newCategoryDto3 = Instancio.create(NewCategoryDto.class);
        newCategoryDto4 = Instancio.create(NewCategoryDto.class);
        newCategoryDto5 = Instancio.create(NewCategoryDto.class);
    }

    @Test
    void addCategoryTest() {
        assertThat(repository.findAll(), empty());

        CategoryDto categoryFromRepository = service.addCategory(newCategoryDto1);

        assertThat(categoryFromRepository.getId(), notNullValue());
        assertThat(newCategoryDto1.getName(), is(categoryFromRepository.getName()));
    }

    @Test
    void updateCategoryTest() {
        Long catId = 1L;
        Integer expectedSize = 1;
        service.addCategory(newCategoryDto1);

        assertThat(repository.findAll().size(), is(expectedSize));

        CategoryDto categoryFromRepository = service.updateCategory(catId, newCategoryDto2);


        assertThat(repository.findAll().size(), is(expectedSize));
        assertThat(categoryFromRepository.getId(), notNullValue());
        assertThat(newCategoryDto2.getName(), is(categoryFromRepository.getName()));
    }

    @Test
    void verifyUpdateCategoryException() {
        Long catId = 1L;
        newCategoryDto1.setName(null);

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> service.updateCategory(catId, newCategoryDto1));

        assertThat("The required object was not found.", is(exception.getMessage()));
    }

    @Test
    void deleteCategoryTest() {
        CategoryDto categoryFromRepository = service.addCategory(newCategoryDto1);

        assertThat(repository.findAll(), notNullValue());

        service.deleteCategory(categoryFromRepository.getId());

        assertThat(repository.findAll(), empty());
    }

    @Test
    void getAllCategoriesTest() {
        assertThat(repository.findAll(), empty());

        Integer expectedSize = 1;
        PageRequest pageRequest = PageRequest.of(2, 2);
        service.addCategory(newCategoryDto1);
        service.addCategory(newCategoryDto2);
        service.addCategory(newCategoryDto3);
        service.addCategory(newCategoryDto4);
        service.addCategory(newCategoryDto5);

        List<CategoryDto> categories = service.getAllCategories(pageRequest);

        assertThat(categories.size(), is(expectedSize));
    }

    @Test
    void getCategory() {
        assertThat(repository.findAll(), empty());

        CategoryDto categoryDto = service.addCategory(newCategoryDto2);

        CategoryDto categoryFromRepository = service.getCategory(categoryDto.getId());

        assertThat(categoryFromRepository.getId(), is(categoryDto.getId()));
        assertThat(categoryFromRepository.getName(), is(categoryDto.getName()));
        assertThat(categoryFromRepository.getName(), is(newCategoryDto2.getName()));
    }

    @Test
    void verifyGetCategoryException() {
        assertThat(repository.findAll(), empty());

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> service.getCategory(1L));

        assertThat("The required object was not found.", is(exception.getMessage()));
    }

    private void resetIdColumns() {
        entityManager
                .createNativeQuery("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
