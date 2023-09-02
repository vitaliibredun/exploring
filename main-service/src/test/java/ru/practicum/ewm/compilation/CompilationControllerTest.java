package ru.practicum.ewm.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.compilation.controller.CompilationController;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompilationController.class)
public class CompilationControllerTest {
    @MockBean
    private CompilationService service;
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    private CompilationDto compilationDto1;
    private CompilationDto compilationDto2;

    @BeforeEach
    void setUp() {
        compilationDto1 = Instancio.create(CompilationDto.class);
        compilationDto2 = Instancio.create(CompilationDto.class);
    }

    @Test
    void getCompilations() throws Exception {
        Integer expectedSize = 2;
        String pinned = "true";
        String from = "0";
        String size = "10";

        when(service.getCompilations(anyBoolean(), any()))
                .thenReturn(List.of(compilationDto1, compilationDto2));

        mvc.perform(get("/compilations")
                        .param("pinned", pinned)
                        .param("from", from)
                        .param("size", size)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("size()", is(expectedSize)))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto1.getPinned())))
                .andExpect(jsonPath("$.[0].title", is(compilationDto1.getTitle())))
                .andExpect(jsonPath("$.[1].pinned", is(compilationDto2.getPinned())))
                .andExpect(jsonPath("$.[1].title", is(compilationDto2.getTitle())));
    }

    @Test
    void getCompilation() throws Exception {
        Integer compId = 2;

        when(service.getCompilation(anyLong()))
                .thenReturn(compilationDto1);

        mvc.perform(get("/compilations/{compId}", compId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned", is(compilationDto1.getPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto1.getTitle())));
    }
}
