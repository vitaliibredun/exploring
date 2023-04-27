package ru.practicum.ewm.service;

import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.EndpointHit;

import java.util.List;

public interface StatsService {
    void saveDataRequest(EndpointHit endpointHit);

    List<ViewStats> getStats(String start, String end, List<String> uris, String unique);
}
