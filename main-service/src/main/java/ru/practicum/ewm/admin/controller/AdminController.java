package ru.practicum.ewm.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.admin.dto.UpdateEventAdmin;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(userDto);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        return userService.getUsers(ids, PageRequest.of(from / size, size));
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto categoryDto) {
        return categoryService.addCategory(categoryDto);
    }

    @PatchMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable Long catId, @Valid @RequestBody NewCategoryDto categoryDto) {
        return categoryService.updateCategory(catId, categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId, @RequestBody UpdateEventAdmin updateEvent) {
        return eventService.updateEventByAdmin(eventId, updateEvent);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> searchForEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        return eventService.searchForEventsByAdmin(
                users, states, categories, rangeStart, rangeEnd, PageRequest.of(from / size, size));
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto compilationDto) {
        return compilationService.addCompilation(compilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Long compId, @Valid @RequestBody UpdateCompilationDto updateCompilationDto) {
        return compilationService.updateCompilation(compId, updateCompilationDto);
    }
}
