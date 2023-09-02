package ru.practicum.ewm.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.request.constants.RequestState;
import ru.practicum.ewm.request.dto.EventRequestResult;
import ru.practicum.ewm.request.dto.EventRequestStatus;
import ru.practicum.ewm.request.dto.RequestToEvent;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.user.controller.UserController;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @MockBean
    private EventService eventService;
    @MockBean
    private RequestService requestService;
    @MockBean
    private CommentService commentService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    private EventFullDto eventFullDto;
    private EventShortDto eventShortDto1;
    private EventShortDto eventShortDto2;
    private RequestToEvent requestToEvent1;
    private RequestToEvent requestToEvent2;
    private EventRequestResult eventRequestResult;
    private NewEventDto newEventDto;
    private UpdateEvent updateEvent;
    private EventRequestStatus eventRequestStatus;

    @BeforeEach
    void setUp() {
        eventFullDto = makeEventFullDto("some title");
        eventShortDto1 = makeEventShortDto("title");
        eventShortDto2 = makeEventShortDto("another title");
        requestToEvent1 = makeRequestToEvent(1L);
        requestToEvent2 = makeRequestToEvent(2L);
        eventRequestResult = makeEventRequestResult();

        newEventDto = Instancio.create(NewEventDto.class);

        updateEvent = makeUpdateEvent("the new one");
        eventRequestStatus = makeEventRequestStatus(1L);
    }

    @Test
    void addEvent() throws Exception {
        Integer userId = 1;

        when(eventService.addEvent(anyLong(), any()))
                .thenReturn(eventFullDto);

        mvc.perform(post("/users/{userId}/events", userId)
                        .content(mapper.writeValueAsString(newEventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate())))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto.getRequestModeration())));
    }

    @Test
    void updateEvent() throws Exception {
        Integer userId = 1;
        Integer eventId = 1;

        when(eventService.updateEvent(anyLong(), anyLong(), any()))
                .thenReturn(eventFullDto);

        mvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(mapper.writeValueAsString(updateEvent))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate())))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto.getRequestModeration())));
    }

    @Test
    void getEvent() throws Exception {
        Integer userId = 1;
        Integer eventId = 1;

        when(eventService.getEvent(anyLong(), anyLong()))
                .thenReturn(eventFullDto);

        mvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate())))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto.getRequestModeration())));
    }

    @Test
    void getAllEventsByUser() throws Exception {
        Integer userId = 1;

        when(eventService.getAllEventsByUser(anyLong(), any()))
                .thenReturn(List.of(eventShortDto1, eventShortDto2));

        mvc.perform(get("/users/{userId}/events", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].annotation", is(eventShortDto1.getAnnotation())))
                .andExpect(jsonPath("$.[1].annotation", is(eventShortDto2.getAnnotation())))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventShortDto1.getConfirmedRequests())))
                .andExpect(jsonPath("$.[1].confirmedRequests", is(eventShortDto2.getConfirmedRequests())))
                .andExpect(jsonPath("$.[0].title", is(eventShortDto1.getTitle())))
                .andExpect(jsonPath("$.[1].title", is(eventShortDto2.getTitle())));
    }

    @Test
    void getRequestsByEvent() throws Exception {
        Integer userId = 1;
        Integer eventId = 1;

        when(requestService.getRequestsByEvent(anyLong(), anyLong()))
                .thenReturn(List.of(requestToEvent1, requestToEvent2));

        mvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].created", is(requestToEvent1.getCreated())))
                .andExpect(jsonPath("$.[1].created", is(requestToEvent2.getCreated())))
                .andExpect(jsonPath("$.[0].status", is(requestToEvent1.getStatus().toString())))
                .andExpect(jsonPath("$.[1].status", is(requestToEvent2.getStatus().toString())));
    }

    @Test
    void updateStatusOfEvent() throws Exception {
        Integer userId = 1;
        Integer eventId = 1;

        when(requestService.updateStatusOfEvent(anyLong(), anyLong(), any()))
                .thenReturn(eventRequestResult);

        mvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .content(mapper.writeValueAsString(eventRequestStatus))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", is(eventRequestResult.getConfirmedRequests())))
                .andExpect(jsonPath("$.rejectedRequests", is(eventRequestResult.getRejectedRequests())));
    }

    @Test
    void addRequestToEvent() throws Exception {
        Integer userId = 1;
        String eventId = "1";

        when(requestService.addRequestToEvent(anyLong(), anyLong()))
                .thenReturn(requestToEvent1);

        mvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created", is(requestToEvent1.getCreated())))
                .andExpect(jsonPath("$.event", is(requestToEvent1.getEvent())))
                .andExpect(jsonPath("$.requester", is(requestToEvent1.getRequester().intValue())))
                .andExpect(jsonPath("$.status", is(requestToEvent1.getStatus().toString())));
    }

    @Test
    void cancelRequestToEvent() throws Exception {
        Integer userId = 1;
        Integer requestId = 1;

        when(requestService.cancelRequestToEvent(anyLong(), anyLong()))
                .thenReturn(requestToEvent1);

        mvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(requestToEvent1.getCreated())))
                .andExpect(jsonPath("$.event", is(requestToEvent1.getEvent())))
                .andExpect(jsonPath("$.requester", is(requestToEvent1.getRequester().intValue())))
                .andExpect(jsonPath("$.status", is(requestToEvent1.getStatus().toString())));
    }

    @Test
    void getAllRequestsToEventsByUser() throws Exception {
        Integer userId = 1;

        when(requestService.getAllRequestsToEventsByUser(anyLong()))
                .thenReturn(List.of(requestToEvent1, requestToEvent2));

        mvc.perform(get("/users/{userId}/requests", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].created", is(requestToEvent1.getCreated())))
                .andExpect(jsonPath("$.[0].event", is(requestToEvent1.getEvent())))
                .andExpect(jsonPath("$.[0].requester", is(requestToEvent1.getRequester().intValue())))
                .andExpect(jsonPath("$.[0].status", is(requestToEvent1.getStatus().toString())))
                .andExpect(jsonPath("$.[1].created", is(requestToEvent2.getCreated())))
                .andExpect(jsonPath("$.[1].event", is(requestToEvent2.getEvent())))
                .andExpect(jsonPath("$.[1].requester", is(requestToEvent2.getRequester().intValue())))
                .andExpect(jsonPath("$.[1].status", is(requestToEvent2.getStatus().toString())));
    }

    private EventRequestStatus makeEventRequestStatus(Long requestId) {
        EventRequestStatus.EventRequestStatusBuilder builder = EventRequestStatus.builder();

        builder.requestIds(List.of(requestId, 2L));
        builder.status(RequestState.PENDING);

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

    private NewEventDto makeNewEventDto(String title) {
        NewEventDto.NewEventDtoBuilder builder = NewEventDto.builder();

        builder.annotation("annotation");
        builder.category(1L);
        builder.paid(true);
        builder.requestModeration(false);
        builder.title(title);

        return builder.build();
    }

    private EventRequestResult makeEventRequestResult() {
        EventRequestResult.EventRequestResultBuilder builder = EventRequestResult.builder();

        return builder.build();
    }

    private RequestToEvent makeRequestToEvent(Long requester) {
        RequestToEvent.RequestToEventBuilder builder = RequestToEvent.builder();

        builder.id(1L);
        builder.requester(requester);
        builder.status(RequestState.PENDING);

        return builder.build();
    }

    private EventShortDto makeEventShortDto(String title) {
        EventShortDto.EventShortDtoBuilder builder = EventShortDto.builder();

        builder.id(1L);
        builder.annotation("annotation");
        builder.category(CategoryDto.builder().id(1L).name("name").build());
        builder.initiator(UserShortDto.builder().id(1L).name("John").build());
        builder.paid(true);
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
}
