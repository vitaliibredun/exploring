package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exceptions.WrongParamUniqueException;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.repository.StatsRepository;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
public class StatsServiceImplTest {
    private final StatsService service;
    private final StatsRepository repository;
    private EndpointHit endpointHit1;
    private EndpointHit endpointHit2;
    private EndpointHit endpointHit3;
    private EndpointHit endpointHit4;

    @BeforeEach
    void setUp() {
        endpointHit1 = makeEndpointHit("/events", "192.168.0.1", LocalDateTime.now().minusDays(2L));

        endpointHit2 = makeEndpointHit("/events/1", "192.168.1.1", LocalDateTime.now().minusDays(5L));

        endpointHit3 = makeEndpointHit("/events/1", "192.168.1.1", LocalDateTime.now().minusDays(10L));

        endpointHit4 = makeEndpointHit("/events", "192.168.1.2", LocalDateTime.now().minusDays(20L));
    }

    @Test
    void saveDataRequestTest() {
        assertThat(repository.findAll(), empty());

        service.saveDataRequest(endpointHit1);
        Stat statFromRepository = repository.findAll().get(0);

        assertThat(repository.findAll(), notNullValue());
        assertThat(statFromRepository.getId(), notNullValue());
        assertThat(endpointHit1.getApp(), is(statFromRepository.getApp()));
        assertThat(endpointHit1.getUri(), is(statFromRepository.getUri()));
        assertThat(endpointHit1.getIp(), is(statFromRepository.getIp()));
        assertThat(endpointHit1.getTimestamp(), is(statFromRepository.getTimestamp()));
    }

    @Test
    void findALLStatsTest() {
        Integer expectedSize = 2;
        Long expectedHits1 = 1L;
        Long expectedHits2 = 2L;

        assertThat(repository.findAll(), empty());

        service.saveDataRequest(endpointHit1);
        service.saveDataRequest(endpointHit2);
        service.saveDataRequest(endpointHit3);
        LocalDateTime startTime = LocalDateTime.of(2022, 12, 1, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 1, 1, 0);
        String start = getString(startTime);
        String end = getString(endTime);

        List<ViewStats> allStats = service.getStats(start, end, null, "false");

        assertThat(repository.findAll(), notNullValue());
        assertThat(allStats.size(), is(expectedSize));
        assertThat(allStats.get(0).getHits(), is(expectedHits2));
        assertThat(allStats.get(1).getHits(), is(expectedHits1));
        assertThat("ewm-main-service", is(allStats.get(0).getApp()));
        assertThat("ewm-main-service", is(allStats.get(1).getApp()));
        assertThat("/events", is(allStats.get(1).getUri()));
        assertThat("/events/1", is(allStats.get(0).getUri()));
    }

    @Test
    void findALLStatsWithUniqueIpTest() {
        Integer expectedSize = 1;
        Long expectedHits = 1L;

        assertThat(repository.findAll(), empty());

        service.saveDataRequest(endpointHit1);
        service.saveDataRequest(endpointHit2);
        service.saveDataRequest(endpointHit3);
        service.saveDataRequest(endpointHit4);
        LocalDateTime startTime = LocalDateTime.of(2022, 12, 1, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 1, 1, 0);
        String start = getString(startTime);
        String end = getString(endTime);

        List<ViewStats> allStats = service.getStats(start, end, null, "true");

        assertThat(repository.findAll(), notNullValue());
        assertThat(allStats.size(), is(expectedSize));
        assertThat(allStats.get(0).getHits(), is(expectedHits));
        assertThat("ewm-main-service", is(allStats.get(0).getApp()));
        assertThat("/events/1", is(allStats.get(0).getUri()));
    }

    @Test
    void findStatsWithUniqueIpTest() {
        Integer expectedSize = 1;
        Long expectedHits = 1L;

        assertThat(repository.findAll(), empty());

        service.saveDataRequest(endpointHit1);
        service.saveDataRequest(endpointHit2);
        service.saveDataRequest(endpointHit3);
        service.saveDataRequest(endpointHit4);
        LocalDateTime startTime = LocalDateTime.of(2022, 12, 1, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 1, 1, 0);
        String start = getString(startTime);
        String end = getString(endTime);

        List<ViewStats> allStats = service.getStats(start, end, List.of("/events", "/events/1"), "true");

        assertThat(repository.findAll(), notNullValue());
        assertThat(allStats.size(), is(expectedSize));
        assertThat(allStats.get(0).getHits(), is(expectedHits));
        assertThat("/events/1", is(allStats.get(0).getUri()));
    }

    @Test
    void findStatsByUriListTest() {
        Integer expectedSize = 2;
        Long expectedHits1 = 1L;
        Long expectedHits2 = 2L;

        assertThat(repository.findAll(), empty());

        service.saveDataRequest(endpointHit1);
        service.saveDataRequest(endpointHit2);
        service.saveDataRequest(endpointHit3);
        LocalDateTime startTime = LocalDateTime.of(2022, 12, 1, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 1, 1, 0);
        String start = getString(startTime);
        String end = getString(endTime);

        List<ViewStats> allStats = service.getStats(start, end, List.of("/events", "/events/1"), "false");

        assertThat(repository.findAll(), notNullValue());
        assertThat(allStats.size(), is(expectedSize));
        assertThat(allStats.get(0).getHits(), is(expectedHits2));
        assertThat(allStats.get(1).getHits(), is(expectedHits1));
        assertThat("ewm-main-service", is(allStats.get(0).getApp()));
        assertThat("ewm-main-service", is(allStats.get(1).getApp()));
        assertThat("/events", is(allStats.get(1).getUri()));
        assertThat("/events/1", is(allStats.get(0).getUri()));
    }

    @Test
    void verifyGetStatsException() {
        LocalDateTime startTime = LocalDateTime.of(2022, 12, 1, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 1, 1, 0);
        String start = getString(startTime);
        String end = getString(endTime);

        final WrongParamUniqueException exception = assertThrows(
                WrongParamUniqueException.class,
                () -> service.getStats(start, end, List.of("/events", "/events/1"), "something"));

        assertThat("The wrong param of the unique", is(exception.getMessage()));
    }

    private String getString(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(formatter);
    }

    private EndpointHit makeEndpointHit(String uri, String ip, LocalDateTime timeStamp) {

        return Instancio.of(EndpointHit.class)
                .set(field(EndpointHit::getApp), "ewm-main-service")
                .set(field(EndpointHit::getUri), uri)
                .set(field(EndpointHit::getIp), ip)
                .set(field(EndpointHit::getTimestamp), timeStamp)
                .create();
    }
}
