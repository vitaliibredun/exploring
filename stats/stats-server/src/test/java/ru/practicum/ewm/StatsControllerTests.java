package ru.practicum.ewm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.controller.StatsController;
import ru.practicum.ewm.service.StatsService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTests {
    @MockBean
    private StatsService service;
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    private EndpointHit endpointHit;
    private ViewStats viewStats1;
    private ViewStats viewStats2;

    @BeforeEach
    void setUp() {
        endpointHit = makeEndpointHit();

        viewStats1 = makeViewStats("/events", 1L);
        viewStats2 = makeViewStats("/events/1", 4L);
    }

    @Test
    void saveDataRequest() throws Exception {
        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getStats() throws Exception {
        Integer expectedSize = 2;

        when(service.getStats(anyString(), anyString(), any(), anyString()))
                .thenReturn(List.of(viewStats1, viewStats2));

        mvc.perform(get("/stats")
                        .param("start", "2023-01-05 10:40:00")
                        .param("end", "2023-12-15 11:30:00")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("size()", is(expectedSize)))
                .andExpect(jsonPath("$.[0].app", is(viewStats1.getApp())))
                .andExpect(jsonPath("$.[0].uri", is(viewStats1.getUri())))
                .andExpect(jsonPath("$.[0].hits", is(viewStats1.getHits().intValue())))
                .andExpect(jsonPath("$.[1].app", is(viewStats2.getApp())))
                .andExpect(jsonPath("$.[1].uri", is(viewStats2.getUri())))
                .andExpect(jsonPath("$.[1].hits", is(viewStats2.getHits().intValue())));
    }

    private ViewStats makeViewStats(String uri, Long hits) {

        return Instancio.of(ViewStats.class)
                .set(field(ViewStats::getApp), "ewm-main-service")
                .set(field(ViewStats::getUri), uri)
                .set(field(ViewStats::getHits), hits)
                .create();
    }

    private EndpointHit makeEndpointHit() {

        return Instancio.of(EndpointHit.class)
                .set(field(EndpointHit::getApp), "ewm-main-service")
                .set(field(EndpointHit::getUri), "/events")
                .set(field(EndpointHit::getIp), "192.168.0.1")
                .set(field(EndpointHit::getTimestamp), LocalDateTime.now().plusDays(10L))
                .create();
    }
}
