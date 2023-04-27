package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.StatsService;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.EndpointHit;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    public void saveDataRequest(@RequestBody EndpointHit endpointHit) {
        service.saveDataRequest(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", required = false, defaultValue = "false") String unique) {

        return service.getStats(start, end, uris, unique);
    }
}
