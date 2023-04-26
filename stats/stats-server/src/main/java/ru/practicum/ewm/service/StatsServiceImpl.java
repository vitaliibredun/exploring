package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exceptions.WrongParamUniqueException;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.mapper.StatsMapper;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service("statsServiceImpl")
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;
    private final StatsMapper mapper;

    @Override
    public EndpointHit saveDataRequest(EndpointHit endpointHit) {
        Stat stat = mapper.toModel(endpointHit);
        Stat statFromRepository = repository.save(stat);
        return mapper.toDto(statFromRepository);
    }

    @Override
    public List<ViewStats> getStats(String start, String end, List<String> uris, String unique) {
        LocalDateTime timeStart = getTime(start);
        LocalDateTime timeEnd = getTime(end);

        boolean allStats = uris == null && unique.equals("false");
        boolean allUnique = uris == null && unique.equals("true");
        boolean allUniqueByUris = unique.equals("true");
        boolean allByUris = unique.equals("false");

        if (allStats) {
            return repository.findALLStats(timeStart, timeEnd);
        }
        if (allUnique) {
            return repository.findALLStatsWithUniqueIp(timeStart, timeEnd);
        }
        if (allUniqueByUris) {
            return repository.findStatsWithUniqueIp(timeStart, timeEnd, uris);
        }
        if (allByUris) {
            return repository.findStatsByUriList(timeStart, timeEnd, uris);
        }
        throw new WrongParamUniqueException("The wrong param of the unique");
    }

    private LocalDateTime getTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, formatter);
    }
}
