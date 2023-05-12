package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatsClient {
    @Value("http://stats-server:9090")
    private String serverUrl;
    private static final String API_SAVE = "/hit";
    private static final String API_GET = "/stats?";
    private static final String START_PARAM = "start=";
    private static final String END_PARAM = "&end=";
    private static final String URIS_PARAM = "&uris=";
    private static final String UNIQUE_PARAM = "&unique=";
    private final RestTemplate restTemplate = new RestTemplate();

    public void saveStats(EndpointHit endpointHit) {
        restTemplate.postForLocation(serverUrl + API_SAVE, endpointHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String startTime = getString(start);
        String endTime = getString(end);

        ResponseEntity<ViewStats[]> stats = restTemplate.getForEntity(serverUrl + API_GET
                + START_PARAM + startTime
                + END_PARAM + endTime
                + URIS_PARAM + uris
                + UNIQUE_PARAM + unique,
                ViewStats[].class);

        return Arrays.asList(Objects.requireNonNull(stats.getBody()));
    }

    private String getString(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(formatter);
    }
}
