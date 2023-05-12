package ru.practicum.ewm.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.admin.controller.AdminController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
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
    private UpdateEvent updateEvent;
    private CompilationDto compilationDto;
    private NewCompilationDto newCompilationDto;

    @BeforeEach
    void setUp() {
        userDto1 = makeUserDto("John", "mail@my.com");
        userDto2 = makeUserDto("Smith", "myemail@ne.com");
        categoryDto = makeCategoryDto("category");
        newCategoryDto = makeNewCategoryDto("category");
        eventFullDto1 = makeEventFullDto("some title");
        eventFullDto2 = makeEventFullDto("another title");
        updateEvent = makeUpdateEvent("the new one");
        compilationDto = makeCompilationDto("title", true);
        newCompilationDto = makeNewCompilationDto("new title", false);
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
                .andExpect(jsonPath("$.eventDate", is(eventFullDto1.getEventDate())))
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
                .andExpect(jsonPath("$.[0].eventDate", is(eventFullDto1.getEventDate())))
                .andExpect(jsonPath("$.[0].requestModeration", is(eventFullDto1.getRequestModeration())))
                .andExpect(jsonPath("$.[1].annotation", is(eventFullDto2.getAnnotation())))
                .andExpect(jsonPath("$.[1].description", is(eventFullDto2.getDescription())))
                .andExpect(jsonPath("$.[1].eventDate", is(eventFullDto2.getEventDate())))
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

    private NewCompilationDto makeNewCompilationDto(String title, Boolean pinned) {
        NewCompilationDto.NewCompilationDtoBuilder builder = NewCompilationDto.builder();

        builder.pinned(pinned);
        builder.title(title);
        builder.events(Set.of(1L, 2L));

        return builder.build();
    }

    private CompilationDto makeCompilationDto(String title, Boolean pinned) {
        CompilationDto.CompilationDtoBuilder builder = CompilationDto.builder();

        builder.id(1L);
        builder.pinned(pinned);
        builder.title(title);
        builder.events(List.of(EventShortDto.builder().build()));

        return builder.build();
    }

    private UpdateEvent makeUpdateEvent(String title) {
        UpdateEvent.UpdateEventBuilder builder = UpdateEvent.builder();

        builder.annotation("annotation");
        builder.category(1L);
        builder.description("description");
        builder.location(LocationDto.builder().lat(55.754167F).lon(37.6232F).build());
        builder.paid(true);
        builder.participantLimit(10L);
        builder.requestModeration(true);
        builder.title(title);

        return builder.build();
    }

    private EventFullDto makeEventFullDto(String title) {
        EventFullDto.EventFullDtoBuilder builder = EventFullDto.builder();

        builder.id(1L);
        builder.annotation("annotation");
        builder.category(CategoryDto.builder().id(1L).name("name").build());
        builder.description("description");
        builder.initiator(UserShortDto.builder().id(1L).name("John").build());
        builder.location(LocationDto.builder().lat(55.754167F).lon(37.6232F).build());
        builder.paid(true);
        builder.participantLimit(10L);
        builder.requestModeration(true);
        builder.state(EventState.PENDING);
        builder.title(title);

        return builder.build();
    }

    private CategoryDto makeCategoryDto(String name) {
        CategoryDto.CategoryDtoBuilder builder = CategoryDto.builder();

        builder.id(1L);
        builder.name(name);

        return builder.build();
    }

    private NewCategoryDto makeNewCategoryDto(String name) {
        NewCategoryDto.NewCategoryDtoBuilder builder = NewCategoryDto.builder();

        builder.name(name);

        return builder.build();
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto.UserDtoBuilder builder = UserDto.builder();

        builder.name(name);
        builder.email(email);

        return builder.build();
    }
}
