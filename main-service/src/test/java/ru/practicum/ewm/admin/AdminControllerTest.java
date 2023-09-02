package ru.practicum.ewm.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.admin.controller.AdminController;
import ru.practicum.ewm.admin.dto.UpdateEventAdmin;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
public class AdminControllerTest {
    @MockBean
    private UserService userService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private EventService eventService;
    @MockBean
    private CompilationService compilationService;
    @MockBean
    private CommentService commentService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    private UserDto userDto1;
    private UserDto userDto2;
    private CategoryDto categoryDto;
    private NewCategoryDto newCategoryDto;
    private EventFullDto eventFullDto1;
    private EventFullDto eventFullDto2;
    private UpdateEventAdmin updateEvent;
    private CompilationDto compilationDto;
    private NewCompilationDto newCompilationDto;

    @BeforeEach
    void setUp() {
        userDto1 = createUserDto("John");
        userDto2 = createUserDto("Timmy");

        categoryDto = Instancio.create(CategoryDto.class);

        newCategoryDto = Instancio.create(NewCategoryDto.class);

        eventFullDto1 = Instancio.create(EventFullDto.class);
        eventFullDto2 = Instancio.create(EventFullDto.class);

        updateEvent = Instancio.create(UpdateEventAdmin.class);

        compilationDto = Instancio.create(CompilationDto.class);

        newCompilationDto = Instancio.create(NewCompilationDto.class);
    }

    @Test
    void addUser() throws Exception {
        when(userService.addUser(any()))
                .thenReturn(userDto1);

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(userDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));
    }

    @Test
    void getUsers() throws Exception {
        String ids = "1,2";
        Integer expectedSize = 2;

        when(userService.getUsers(any(), any()))
                .thenReturn(List.of(userDto1, userDto2));

        mvc.perform(get("/admin/users")
                        .param("ids", ids)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("size()", is(expectedSize)))
                .andExpect(jsonPath("$.[0].name", is(userDto1.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto1.getEmail())))
                .andExpect(jsonPath("$.[1].name", is(userDto2.getName())))
                .andExpect(jsonPath("$.[1].email", is(userDto2.getEmail())));
    }

    @Test
    void deleteUser() throws Exception {
        Integer userId = 1;

        mvc.perform(delete("/admin/users/{userId}", userId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void addCategory() throws Exception {
        when(categoryService.addCategory(any()))
                .thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoryDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));
    }

    @Test
    void updateCategory() throws Exception {
        Integer catId = 1;

        when(categoryService.updateCategory(anyLong(), any()))
                .thenReturn(categoryDto);

        mvc.perform(patch("/admin/categories/{catId}", catId)
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));
    }

    @Test
    void deleteCategory() throws Exception {
        Integer catId = 1;

        mvc.perform(delete("/admin/categories/{catId}", catId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateEventByAdmin() throws Exception {
        Integer eventId = 1;

        when(eventService.updateEventByAdmin(anyLong(), any()))
                .thenReturn(eventFullDto1);

        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .content(mapper.writeValueAsString(updateEvent))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation", is(eventFullDto1.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventFullDto1.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(getTimeFormat(eventFullDto1.getEventDate()))))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto1.getRequestModeration())));
    }

    @Test
    void searchForEventsByAdmin() throws Exception {
        String users = "1,2";
        String states = "PENDING,PUBLISHED";
        String categories = "3,2";
        String rangeStart = "2022-01-06%2013%3A30%3A38";
        String rangeEnd = "2097-09-06%2013%3A30%3A38";
        String from = "0";
        String size = "10";
        Integer expectedSize = 2;

        when(eventService.searchForEventsByAdmin(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(eventFullDto1, eventFullDto2));

        mvc.perform(get("/admin/events")
                        .param("users", users)
                        .param("states", states)
                        .param("categories", categories)
                        .param("rangeStart", rangeStart)
                        .param("rangeEnd", rangeEnd)
                        .param("from", from)
                        .param("size", size)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("size()", is(expectedSize)))
                .andExpect(jsonPath("$.[0].annotation", is(eventFullDto1.getAnnotation())))
                .andExpect(jsonPath("$.[0].description", is(eventFullDto1.getDescription())))
                .andExpect(jsonPath("$.[0].eventDate", is(getTimeFormat(eventFullDto1.getEventDate()))))
                .andExpect(jsonPath("$.[0].requestModeration", is(eventFullDto1.getRequestModeration())))
                .andExpect(jsonPath("$.[1].annotation", is(eventFullDto2.getAnnotation())))
                .andExpect(jsonPath("$.[1].description", is(eventFullDto2.getDescription())))
                .andExpect(jsonPath("$.[1].eventDate", is(getTimeFormat(eventFullDto2.getEventDate()))))
                .andExpect(jsonPath("$.[1].requestModeration", is(eventFullDto2.getRequestModeration())));
    }

    @Test
    void addCompilation() throws Exception {
        when(compilationService.addCompilation(any()))
                .thenReturn(compilationDto);

        mvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newCompilationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(compilationDto.getId().intValue())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())));
    }

    @Test
    void deleteCompilation() throws Exception {
        Integer compId = 1;

        mvc.perform(delete("/admin/compilations/{compId}", compId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCompilation() throws Exception {
        Integer compId = 1;

        when(compilationService.updateCompilation(anyLong(), any()))
                .thenReturn(compilationDto);

        mvc.perform(patch("/admin/compilations/{compId}", compId)
                        .content(mapper.writeValueAsString(newCompilationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationDto.getId().intValue())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())));
    }

    private String getTimeFormat(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(dateTimeFormatter);
    }

    private UserDto createUserDto(String name) {
        return Instancio.of(UserDto.class)
                .set(field(UserDto::getName), name)
                .set(field(UserDto::getEmail), "user@gmail.com")
                .create();
    }
}
