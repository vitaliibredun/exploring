package ru.practicum.ewm.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.controller.CategoryController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
public class CategoryControllerTest {
    @MockBean
    private CategoryService service;
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    private CategoryDto categoryDto1;
    private CategoryDto categoryDto2;

    @BeforeEach
    void setUp() {
        categoryDto1 = Instancio.create(CategoryDto.class);
        categoryDto2 = Instancio.create(CategoryDto.class);
    }

    @Test
    void getAllCategories() throws Exception {
        when(service.getAllCategories(any()))
                .thenReturn(List.of(categoryDto1, categoryDto2));

        mvc.perform(get("/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(categoryDto1.getId().intValue())))
                .andExpect(jsonPath("$.[0].name", is(categoryDto1.getName())))
                .andExpect(jsonPath("$.[1].id", is(categoryDto2.getId().intValue())))
                .andExpect(jsonPath("$.[1].name", is(categoryDto2.getName())));
    }

    @Test
    void getCategory() throws Exception {
        Integer catId = 1;

        when(service.getCategory(anyLong()))
                .thenReturn(categoryDto1);

        mvc.perform(get("/categories/{catId}", catId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto1.getId().intValue())))
                .andExpect(jsonPath("$.name", is(categoryDto1.getName())));
    }
}
