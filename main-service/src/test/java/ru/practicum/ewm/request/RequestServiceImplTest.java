package ru.practicum.ewm.request;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.request.constants.RequestState;
import ru.practicum.ewm.request.dto.EventRequestResult;
import ru.practicum.ewm.request.dto.EventRequestStatus;
import ru.practicum.ewm.request.dto.RequestToEvent;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
public class RequestServiceImplTest {
    private final RequestService requestService;
    private final RequestRepository repository;
    private final EventService eventService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final EntityManager entityManager;
    private NewEventDto newEventDto1;
    private NewEventDto newEventDto2;
    private NewEventDto newEventDto3;
    private Event event1;
    private Event event2;
    private Event event3;

    @BeforeEach
    void setUp() {
        resetIdColumns();

        NewCategoryDto newCategoryDto = Instancio.create(NewCategoryDto.class);

        categoryService.addCategory(newCategoryDto);

        UserDto userDto1 = Instancio.of(UserDto.class).ignore(field(UserDto::getId)).create();
        UserDto userDto2 = Instancio.of(UserDto.class).ignore(field(UserDto::getId)).create();
        UserDto userDto3 = Instancio.of(UserDto.class).ignore(field(UserDto::getId)).create();
        UserDto userDto4 = Instancio.of(UserDto.class).ignore(field(UserDto::getId)).create();

        UserDto user1 = userService.addUser(userDto1);

        userService.addUser(userDto2);
        userService.addUser(userDto3);
        userService.addUser(userDto4);

        newEventDto1 = makeNewEventDto("title");
        newEventDto2 = makeNewEventDto("another title");
        newEventDto3 = makeNewEventDto("the last title");

        eventService.addEvent(user1.getId(), newEventDto1);
        eventService.addEvent(user1.getId(), newEventDto2);
        eventService.addEvent(user1.getId(), newEventDto3);

        event1 = eventRepository.findById(1L).orElseThrow();
        event2 = eventRepository.findById(2L).orElseThrow();
        event3 = eventRepository.findById(3L).orElseThrow();
    }

    @Test
    void addRequestToEventTest() {
        assertThat(repository.findAll(), empty());

        Long userId = 2L;
        Long eventId = 1L;
        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEventFromRepository = requestService.addRequestToEvent(userId, eventId);

        assertThat(requestToEventFromRepository.getId(), notNullValue());
        assertThat(requestToEventFromRepository.getCreated(), notNullValue());
        assertThat(requestToEventFromRepository.getStatus(), is(RequestState.PENDING));
        assertThat(eventId, is(requestToEventFromRepository.getEvent()));
        assertThat(userId, is(requestToEventFromRepository.getRequester()));
    }

    @Test
    void addRequestToEventWithNoLimitTest() {
        assertThat(repository.findAll(), empty());

        Long userId = 2L;
        Long eventId = 1L;
        event1.setState(EventState.PUBLISHED);
        event1.setParticipantLimit(0L);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEventFromRepository = requestService.addRequestToEvent(userId, eventId);

        assertThat(requestToEventFromRepository.getId(), notNullValue());
        assertThat(requestToEventFromRepository.getCreated(), notNullValue());
        assertThat(requestToEventFromRepository.getStatus(), is(RequestState.CONFIRMED));
        assertThat(eventId, is(requestToEventFromRepository.getEvent()));
        assertThat(userId, is(requestToEventFromRepository.getRequester()));
    }

    @Test
    void addRequestToEventWithNoModerationTest() {
        assertThat(repository.findAll(), empty());

        Long userId = 2L;
        Long eventId = 1L;
        event1.setState(EventState.PUBLISHED);
        event1.setRequestModeration(false);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEventFromRepository = requestService.addRequestToEvent(userId, eventId);

        assertThat(requestToEventFromRepository.getId(), notNullValue());
        assertThat(requestToEventFromRepository.getCreated(), notNullValue());
        assertThat(requestToEventFromRepository.getStatus(), is(RequestState.CONFIRMED));
        assertThat(eventId, is(requestToEventFromRepository.getEvent()));
        assertThat(userId, is(requestToEventFromRepository.getRequester()));
    }

    @Test
    void verifyAddRequestWithLimitReachedException() {
        Long userId = 2L;
        Long eventId = 1L;
        Long userIdAboveLimit = 3L;
        Long requestId = 1L;
        event1.setState(EventState.PUBLISHED);
        event1.setParticipantLimit(1L);
        eventRepository.saveAndFlush(event1);
        requestService.addRequestToEvent(userId, eventId);
        Request request = repository.findById(requestId).orElseThrow();
        request.setStatus(RequestState.CONFIRMED);
        repository.saveAndFlush(request);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.addRequestToEvent(userIdAboveLimit, eventId));

        assertThat("The participant limit has been reached", is(exception.getMessage()));
    }

    @Test
    void verifyAddRequestNotPublishedEventException() {
        Long userId = 2L;
        Long eventId = 1L;

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.addRequestToEvent(userId, eventId));

        assertThat("The event is not published yet", is(exception.getMessage()));
    }

    @Test
    void verifyAddRequestIsOwnerOfEventException() {
        Long ownerOfEvent = 1L;
        Long eventId = 1L;

        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.addRequestToEvent(ownerOfEvent, eventId));

        assertThat("The requester is owner of the event", is(exception.getMessage()));
    }

    @Test
    void verifyAddRequestIsAlreadyExistException() {
        Long userId = 2L;
        Long eventId = 1L;

        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        requestService.addRequestToEvent(userId, eventId);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.addRequestToEvent(userId, eventId));

        assertThat("The request to the event is already exist", is(exception.getMessage()));
    }

    @Test
    void cancelRequestToEventTest() {
        Long userId = 2L;
        Long eventId = 1L;
        Long requestId = 1L;
        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEventBeforeCancelled = requestService.addRequestToEvent(userId, eventId);

        RequestToEvent requestToEventCancelled = requestService.cancelRequestToEvent(userId, requestId);

        assertThat(requestToEventBeforeCancelled.getId(), is(requestToEventCancelled.getId()));
        assertThat(requestToEventBeforeCancelled.getStatus(), is(RequestState.PENDING));
        assertThat(requestToEventCancelled.getStatus(), is(RequestState.CANCELED));
    }

    @Test
    void verifyCancelRequestExistException() {
        Long userId = 2L;
        Long eventId = 1L;
        Long wrongRequestId = 10L;

        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        requestService.addRequestToEvent(userId, eventId);

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> requestService.cancelRequestToEvent(userId, wrongRequestId));

        assertThat("The request doesn't exist", is(exception.getMessage()));
    }

    @Test
    void verifyCancelRequestWrongOwnerException() {
        Long userId = 2L;
        Long eventId = 1L;
        Long requestId = 1L;
        Long wrongRequester = 10L;

        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        requestService.addRequestToEvent(userId, eventId);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.cancelRequestToEvent(wrongRequester, requestId));

        assertThat("The user is not owner of the request", is(exception.getMessage()));
    }

    @Test
    void getAllRequestsToEventsByUserTest() {
        Long userId = 2L;
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        Long eventId3 = 3L;
        event1.setState(EventState.PUBLISHED);
        event2.setState(EventState.PUBLISHED);
        event3.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        eventRepository.saveAndFlush(event2);
        eventRepository.saveAndFlush(event3);
        RequestToEvent requestToEvent1 = requestService.addRequestToEvent(userId, eventId1);
        RequestToEvent requestToEvent2 = requestService.addRequestToEvent(userId, eventId2);
        RequestToEvent requestToEvent3 = requestService.addRequestToEvent(userId, eventId3);

        List<RequestToEvent> allRequests = requestService.getAllRequestsToEventsByUser(userId);
        RequestToEvent requestToEventFromRepository1 = allRequests.get(0);
        RequestToEvent requestToEventFromRepository2 = allRequests.get(1);
        RequestToEvent requestToEventFromRepository3 = allRequests.get(2);

        assertThat(requestToEvent1.getEvent(), is(requestToEventFromRepository1.getEvent()));
        assertThat(requestToEvent1.getRequester(), is(requestToEventFromRepository1.getRequester()));
        assertThat(requestToEvent1.getStatus(), is(requestToEventFromRepository1.getStatus()));
        assertThat(requestToEvent2.getEvent(), is(requestToEventFromRepository2.getEvent()));
        assertThat(requestToEvent2.getRequester(), is(requestToEventFromRepository2.getRequester()));
        assertThat(requestToEvent2.getStatus(), is(requestToEventFromRepository2.getStatus()));
        assertThat(requestToEvent3.getEvent(), is(requestToEventFromRepository3.getEvent()));
        assertThat(requestToEvent3.getRequester(), is(requestToEventFromRepository3.getRequester()));
        assertThat(requestToEvent3.getStatus(), is(requestToEventFromRepository3.getStatus()));
    }

    @Test
    void verifyGetAllRequestsUserNotExistException() {
        Long wrongUserId = 10L;

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> requestService.getAllRequestsToEventsByUser(wrongUserId));

        assertThat("User with id was not found", is(exception.getMessage()));
    }

    @Test
    void getRequestsByEventTest() {
        Long ownerId = 1L;
        Long userId1 = 2L;
        Long userId2 = 3L;
        Long userId3 = 4L;
        Long eventId = 1L;
        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEvent1 = requestService.addRequestToEvent(userId1, eventId);
        RequestToEvent requestToEvent2 = requestService.addRequestToEvent(userId2, eventId);
        RequestToEvent requestToEvent3 = requestService.addRequestToEvent(userId3, eventId);

        List<RequestToEvent> allRequests = requestService.getRequestsByEvent(ownerId, eventId);
        RequestToEvent requestToEventFromRepository1 = allRequests.get(0);
        RequestToEvent requestToEventFromRepository2 = allRequests.get(1);
        RequestToEvent requestToEventFromRepository3 = allRequests.get(2);

        assertThat(requestToEvent1.getEvent(), is(requestToEventFromRepository1.getEvent()));
        assertThat(requestToEvent1.getRequester(), is(requestToEventFromRepository1.getRequester()));
        assertThat(requestToEvent1.getStatus(), is(requestToEventFromRepository1.getStatus()));
        assertThat(requestToEvent2.getEvent(), is(requestToEventFromRepository2.getEvent()));
        assertThat(requestToEvent2.getRequester(), is(requestToEventFromRepository2.getRequester()));
        assertThat(requestToEvent2.getStatus(), is(requestToEventFromRepository2.getStatus()));
        assertThat(requestToEvent3.getEvent(), is(requestToEventFromRepository3.getEvent()));
        assertThat(requestToEvent3.getRequester(), is(requestToEventFromRepository3.getRequester()));
        assertThat(requestToEvent3.getStatus(), is(requestToEventFromRepository3.getStatus()));
    }

    @Test
    void verifyGetRequestsByEventNotOwnerException() {
        Long notOwnerId = 10L;
        Long eventId = 1L;

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.getRequestsByEvent(notOwnerId, eventId));

        assertThat("The user is not owner of the event", is(exception.getMessage()));
    }

    @Test
    void updateStatusOfEventRejectedTest() {
        Long ownerId = 1L;
        Long userId1 = 2L;
        Long userId2 = 3L;
        Long userId3 = 4L;
        Long eventId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        Long requestId3 = 3L;
        event1.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEvent1 = requestService.addRequestToEvent(userId1, eventId);
        RequestToEvent requestToEvent2 = requestService.addRequestToEvent(userId2, eventId);
        RequestToEvent requestToEvent3 = requestService.addRequestToEvent(userId3, eventId);
        RequestState confirmed = RequestState.REJECTED;
        List<Long> requestIds = List.of(requestId1, requestId2, requestId3);
        EventRequestStatus eventRequestStatus = makeEventRequestStatus(requestIds, confirmed);

        EventRequestResult eventRequestResult = requestService.updateStatusOfEvent(ownerId, eventId, eventRequestStatus);
        List<RequestToEvent> confirmedRequests = eventRequestResult.getConfirmedRequests();
        List<RequestToEvent> rejectedRequests = eventRequestResult.getRejectedRequests();
        RequestToEvent requestToEventFromRepository1 = rejectedRequests.get(0);
        RequestToEvent requestToEventFromRepository2 = rejectedRequests.get(1);
        RequestToEvent requestToEventFromRepository3 = rejectedRequests.get(2);

        assertThat(confirmedRequests, empty());
        assertThat(requestToEvent1.getId(), is(requestToEventFromRepository1.getId()));
        assertThat(requestToEventFromRepository1.getStatus(), is(RequestState.REJECTED));
        assertThat(requestToEvent2.getId(), is(requestToEventFromRepository2.getId()));
        assertThat(requestToEventFromRepository2.getStatus(), is(RequestState.REJECTED));
        assertThat(requestToEvent3.getId(), is(requestToEventFromRepository3.getId()));
        assertThat(requestToEventFromRepository3.getStatus(), is(RequestState.REJECTED));
    }

    @Test
    void updateStatusOfEventWithLimitTest() {
        Long ownerId = 1L;
        Long userId1 = 2L;
        Long userId2 = 3L;
        Long userId3 = 4L;
        Long eventId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        Long requestId3 = 3L;
        int expectedConfirmedSize = 2;
        int expectedRejectedSize = 1;
        event1.setState(EventState.PUBLISHED);
        event1.setParticipantLimit(2L);
        eventRepository.saveAndFlush(event1);
        RequestToEvent requestToEvent1 = requestService.addRequestToEvent(userId1, eventId);
        RequestToEvent requestToEvent2 = requestService.addRequestToEvent(userId2, eventId);
        RequestToEvent requestToEvent3 = requestService.addRequestToEvent(userId3, eventId);
        RequestState confirmed = RequestState.CONFIRMED;
        List<Long> requestIds = List.of(requestId1, requestId2, requestId3);
        EventRequestStatus eventRequestStatus = makeEventRequestStatus(requestIds, confirmed);

        EventRequestResult eventRequestResult = requestService.updateStatusOfEvent(ownerId, eventId, eventRequestStatus);
        List<RequestToEvent> confirmedRequests = eventRequestResult.getConfirmedRequests();
        List<RequestToEvent> rejectedRequests = eventRequestResult.getRejectedRequests();
        RequestToEvent confirmedRequest1 = confirmedRequests.get(0);
        RequestToEvent confirmedRequest2 = confirmedRequests.get(1);
        RequestToEvent rejectedRequest = rejectedRequests.get(0);

        assertThat(confirmedRequests.size(), is(expectedConfirmedSize));
        assertThat(rejectedRequests.size(), is(expectedRejectedSize));
        assertThat(requestToEvent1.getId(), is(confirmedRequest1.getId()));
        assertThat(confirmedRequest1.getStatus(), is(RequestState.CONFIRMED));
        assertThat(requestToEvent2.getId(), is(confirmedRequest2.getId()));
        assertThat(confirmedRequest2.getStatus(), is(RequestState.CONFIRMED));
        assertThat(requestToEvent3.getId(), is(rejectedRequest.getId()));
        assertThat(rejectedRequest.getStatus(), is(RequestState.REJECTED));
    }

    @Test
    void verifyUpdateStatusOfEventUserExistException() {
        Long userId = 10L;
        Long eventId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        Long requestId3 = 3L;
        RequestState confirmed = RequestState.CONFIRMED;
        List<Long> requestIds = List.of(requestId1, requestId2, requestId3);
        EventRequestStatus eventRequestStatus = makeEventRequestStatus(requestIds, confirmed);

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> requestService.updateStatusOfEvent(userId, eventId, eventRequestStatus));

        assertThat("User with id was not found", is(exception.getMessage()));
    }

    @Test
    void verifyUpdateStatusOfEventUserNotOwnerException() {
        Long notOwner = 3L;
        Long eventId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        Long requestId3 = 3L;
        RequestState confirmed = RequestState.CONFIRMED;
        List<Long> requestIds = List.of(requestId1, requestId2, requestId3);
        EventRequestStatus eventRequestStatus = makeEventRequestStatus(requestIds, confirmed);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.updateStatusOfEvent(notOwner, eventId, eventRequestStatus));

        assertThat("The user is not owner of the event", is(exception.getMessage()));
    }

    @Test
    void verifyUpdateStatusOfEventStatusRequestsException() {
        Long ownerId = 1L;
        Long userId1 = 2L;
        Long userId2 = 3L;
        Long eventId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        event1.setState(EventState.PUBLISHED);
        event1.setParticipantLimit(2L);
        eventRepository.saveAndFlush(event1);
        requestService.addRequestToEvent(userId1, eventId);
        requestService.addRequestToEvent(userId2, eventId);
        Request request = repository.findById(requestId2).orElseThrow();
        request.setStatus(RequestState.CANCELED);
        repository.saveAndFlush(request);
        RequestState confirmed = RequestState.CONFIRMED;
        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatus eventRequestStatus = makeEventRequestStatus(requestIds, confirmed);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> requestService.updateStatusOfEvent(ownerId, eventId, eventRequestStatus));

        assertThat("Request must have status PENDING", is(exception.getMessage()));
    }

    private EventRequestStatus makeEventRequestStatus(List<Long> requestIds, RequestState status) {
        EventRequestStatus.EventRequestStatusBuilder builder = EventRequestStatus.builder();

        builder.requestIds(requestIds);
        builder.status(status);

        return builder.build();
    }

    private NewEventDto makeNewEventDto(String title) {
        NewEventDto.NewEventDtoBuilder builder = NewEventDto.builder();

        builder.annotation("annotation");
        builder.eventDate(LocalDateTime.now().plusDays(1));
        builder.location(LocationDto.builder().lat(38.3234F).lon(84.342F).build());
        builder.category(1L);
        builder.description("description");
        builder.paid(true);
        builder.participantLimit(5L);
        builder.requestModeration(true);
        builder.title(title);

        return builder.build();
    }

    private void resetIdColumns() {
        entityManager
                .createNativeQuery("ALTER TABLE requests ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE events ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
