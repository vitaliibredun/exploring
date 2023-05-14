package ru.practicum.ewm.compilation.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        return service.getCompilations(pinned, PageRequest.of(from / size, size));
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return service.getCompilation(compId);
    }
}
