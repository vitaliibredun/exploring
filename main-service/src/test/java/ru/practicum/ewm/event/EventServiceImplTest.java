package ru.practicum.ewm.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.admin.constants.StateAction;
import ru.practicum.ewm.admin.dto.UpdateEventAdmin;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
public class EventServiceImplTest {
    private final EventService eventService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final EventRepository repository;
    private final EntityManager entityManager;
    private final EventMapper mapper;
    private NewEventDto newEventDto1;
    private NewEventDto newEventDto2;
    private NewEventDto newEventDto3;
    private NewEventDto newEventDto4;
    private NewEventDto newEventDto5;
    private NewEventDto newEventDto6;
    private UpdateEvent updateEvent;
    private UpdateEventAdmin updateEventAdmin;

    @BeforeEach
    void setUp() {
        NewCategoryDto newCategoryDto1 = makeCategory("travel");
        NewCategoryDto newCategoryDto2 = makeCategory("sport");
        NewCategoryDto newCategoryDto3 = makeCategory("party");
        categoryService.addCategory(newCategoryDto1);
        categoryService.addCategory(newCategoryDto2);
        categoryService.addCategory(newCategoryDto3);
        UserDto userDto1 = makeUserDto("John", "my@mail.com");
        UserDto userDto2 = makeUserDto("Smith", "mail@email.com");
        UserDto userDto3 = makeUserDto("Timmy", "timmy@timmy.com");
        userService.addUser(userDto1);
        userService.addUser(userDto2);
        userService.addUser(userDto3);

        newEventDto1 = makeNewEventDto("title", 1L);
        newEventDto2 = makeNewEventDto("another title", 1L);
        newEventDto3 = makeNewEventDto("another one", 2L);
        newEventDto4 = makeNewEventDto("another title one", 2L);
        newEventDto5 = makeNewEventDto("the last one", 3L);
        newEventDto6 = makeNewEventDto("the last title", 3L);
        updateEvent = makeUpdateEvent("new title", "new annotation");
        updateEventAdmin = makeUpdateEventAdmin("changed title", "changed annotation");

        resetIdColumns();
    }

    @Test
    void addEventTest() {
        assertThat(repository.findAll(), empty());

        Long userId = 1L;
        EventFullDto eventFromRepository = eventService.addEvent(userId, newEventDto1);

        assertThat(eventFromRepository.getId(), notNullValue());
        assertThat(eventFromRepository.getInitiator().getId(), is(userId));
        assertThat(eventFromRepository.getPublishedOn(), nullValue());
        assertThat(eventFromRepository.getPublishedOn(), nullValue());
        assertThat(eventFromRepository.getConfirmedRequests(), nullValue());
        assertThat(eventFromRepository.getViews(), nullValue());
        assertThat(newEventDto1.getAnnotation(), is(eventFromRepository.getAnnotation()));
        assertThat(newEventDto1.getCategory(), is(eventFromRepository.getCategory().getId()));
        assertThat(newEventDto1.getPaid(), is(eventFromRepository.getPaid()));
        assertThat(newEventDto1.getTitle(), is(eventFromRepository.getTitle()));
        assertThat(newEventDto1.getLocation().getLatitude(), is(eventFromRepository.getLocation().getLatitude()));
        assertThat(newEventDto1.getLocation().getLongitude(), is(eventFromRepository.getLocation().getLongitude()));
    }

    @Test
    void verifyCategoryNotExistsException() {
        Long userId = 1L;
        newEventDto1.setCategory(100L);

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> eventService.addEvent(userId, newEventDto1));

        assertThat("The required object was not found.", is(exception.getMessage()));
    }

    @Test
    void verifyTimeOfEventException() {
        Long userId = 1L;
        newEventDto1.setEventDate(LocalDateTime.now().plusHours(1));

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.addEvent(userId, newEventDto1));

        assertThat("The time of event less then 2 hours from current time", is(exception.getMessage()));
    }

    @Test
    void updateEventTest() {
        Long userId = 1L;
        Long eventId = 1L;
        Integer expectedSize = 1;
        eventService.addEvent(userId, newEventDto1);
        Event event = repository.findById(eventId).orElseThrow();
        EventFullDto firstVersionEvent = mapper.toDto(event);

        assertThat(repository.findAll().size(), is(expectedSize));

        EventFullDto newVersionEvent = eventService.updateEvent(userId, eventId, updateEvent);

        assertThat(repository.findAll().size(), is(expectedSize));
        assertThat(newVersionEvent.getId(), notNullValue());
        assertThat(firstVersionEvent.getAnnotation(), not(newVersionEvent.getAnnotation()));
        assertThat(updateEvent.getAnnotation(), is(newVersionEvent.getAnnotation()));
        assertThat(firstVersionEvent.getEventDate(), not(newVersionEvent.getEventDate()));
        assertThat(updateEvent.getEventDate(), is(newVersionEvent.getEventDate()));
        assertThat(firstVersionEvent.getPaid(), not(newVersionEvent.getPaid()));
        assertThat(updateEvent.getPaid(), is(newVersionEvent.getPaid()));
        assertThat(firstVersionEvent.getRequestModeration(), is(newVersionEvent.getRequestModeration()));
        assertThat(firstVersionEvent.getCategory().getId(), is(newVersionEvent.getCategory().getId()));
    }

    @Test
    void verifyTimeOfUpdateEventException() {
        Long userId = 1L;
        Long eventId = 1L;
        updateEvent.setEventDate(LocalDateTime.now().plusHours(1));

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.updateEvent(userId, eventId, updateEvent));

        assertThat("The time of event less then 2 hours from current time", is(exception.getMessage()));
    }

    @Test
    void verifyEventForUpdateNotFoundException() {
        Long userId = 1L;
        Long eventId = 100L;

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> eventService.updateEvent(userId, eventId, updateEvent));

        assertThat("Event with id was not found", is(exception.getMessage()));
    }

    //    @Test
    //        void verifyNotOwnerOfEventException() {
    //        Long userId = 1L;
    //        Long eventId = 1L;
    //        Long notOwner = 100L;
    //        eventService.addEvent(userId, newEventDto1);
    //
    //        final ConflictException exception = assertThrows(
    //                ConflictException.class,
    //                () -> eventService.updateEvent(notOwner, eventId, updateEvent));
    //
    //        assertThat("Only owner of event can makes changes", is(exception.getMessage()));
    //    }

    @Test
    void verifyChangePublishedEventException() {
        Long userId = 1L;
        Long eventId = 1L;
        eventService.addEvent(userId, newEventDto1);
        Event event = repository.findById(eventId).orElseThrow();
        event.setState(EventState.PUBLISHED);
        repository.saveAndFlush(event);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.updateEvent(userId, eventId, updateEvent));

        assertThat("Only pending or canceled events can be changed", is(exception.getMessage()));
    }

    @Test
    void getEventTest() {
        Long userId = 1L;
        Long eventId = 1L;
        eventService.addEvent(userId, newEventDto1);

        EventFullDto eventFromRepository = eventService.getEvent(userId, eventId);

        assertThat(eventFromRepository.getId(), notNullValue());
        assertThat(newEventDto1.getAnnotation(), is(eventFromRepository.getAnnotation()));
        assertThat(newEventDto1.getCategory(), is(eventFromRepository.getCategory().getId()));
        assertThat(newEventDto1.getPaid(), is(eventFromRepository.getPaid()));
        assertThat(newEventDto1.getTitle(), is(eventFromRepository.getTitle()));
        assertThat(newEventDto1.getLocation().getLatitude(), is(eventFromRepository.getLocation().getLatitude()));
        assertThat(newEventDto1.getLocation().getLongitude(), is(eventFromRepository.getLocation().getLongitude()));
    }

    @Test
    void verifyEventNotFoundException() {
        Long userId = 1L;
        Long eventId = 100L;

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> eventService.getEvent(userId, eventId));

        assertThat("Event with id was not found", is(exception.getMessage()));
    }

    @Test
    void verifyNotOwnerGetEventException() {
        Long userId = 1L;
        Long eventId = 1L;
        Long notOwner = 100L;
        eventService.addEvent(userId, newEventDto1);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.getEvent(notOwner, eventId));

        assertThat("Only owner of event can makes changes", is(exception.getMessage()));
    }

    @Test
    void getAllEventsByUserTest() {
        Integer expectedSize = 1;
        Long userId = 1L;
        PageRequest pageRequest = PageRequest.of(2, 2);
        eventService.addEvent(userId, newEventDto1);
        eventService.addEvent(userId, newEventDto2);
        eventService.addEvent(userId, newEventDto3);
        eventService.addEvent(userId, newEventDto4);
        eventService.addEvent(userId, newEventDto5);

        List<EventShortDto> allEvents = eventService.getAllEventsByUser(userId, pageRequest);
        EventShortDto eventFromRepository = allEvents.get(0);

        assertThat(allEvents.size(), is(expectedSize));
        assertThat(newEventDto5.getAnnotation(), is(eventFromRepository.getAnnotation()));
        assertThat(newEventDto5.getCategory(), is(eventFromRepository.getCategory().getId()));
        assertThat(newEventDto5.getEventDate(), is(eventFromRepository.getEventDate()));
        assertThat(newEventDto5.getTitle(), is(eventFromRepository.getTitle()));
    }

    @Test
    void updateEventByAdminTest() {
        Long userId = 1L;
        Long eventId = 1L;
        Integer expectedSize = 1;
        eventService.addEvent(userId, newEventDto1);
        Event event = repository.findById(eventId).orElseThrow();
        EventFullDto firstVersionEvent = mapper.toDto(event);

        assertThat(repository.findAll().size(), is(expectedSize));

        EventFullDto newVersionEvent = eventService.updateEventByAdmin(eventId, updateEventAdmin);

        assertThat(repository.findAll().size(), is(expectedSize));
        assertThat(newVersionEvent.getId(), is(firstVersionEvent.getId()));
        assertThat(firstVersionEvent.getAnnotation(), not(newVersionEvent.getAnnotation()));
        assertThat(firstVersionEvent.getTitle(), not(newVersionEvent.getTitle()));
        assertThat(firstVersionEvent.getPaid(), is(newVersionEvent.getPaid()));
        assertThat(firstVersionEvent.getRequestModeration(), is(newVersionEvent.getRequestModeration()));
        assertThat(firstVersionEvent.getCategory().getId(), is(newVersionEvent.getCategory().getId()));
    }

    @Test
    void verifyEventForUpdateByAdminNotFoundException() {
        Long eventId = 100L;

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> eventService.updateEventByAdmin(eventId, updateEventAdmin));

        assertThat("Event with id was not found", is(exception.getMessage()));
    }

    @Test
    void verifyEventForUpdateByAdminCanNotRejectException() {
        Long userId = 1L;
        Long eventId = 1L;
        eventService.addEvent(userId, newEventDto1);
        Event event = repository.findById(eventId).orElseThrow();
        event.setState(EventState.PUBLISHED);
        repository.saveAndFlush(event);
        updateEventAdmin.setStateAction(StateAction.REJECT_EVENT);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.updateEventByAdmin(eventId, updateEventAdmin));

        assertThat("Cannot reject the event because it had been published", is(exception.getMessage()));
    }

    @Test
    void verifyEventForUpdateByAdminTimeException() {
        Long userId = 1L;
        Long eventId = 1L;
        long lessThanAnHourBeforePublicationTime = 30L;
        eventService.addEvent(userId, newEventDto1);
        Event event = repository.findById(eventId).orElseThrow();
        event.setEventDate(LocalDateTime.now().plusMinutes(lessThanAnHourBeforePublicationTime));
        repository.saveAndFlush(event);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.updateEventByAdmin(eventId, updateEventAdmin));

        assertThat("EventDate of the event is less than 1 hour before publication time", is(exception.getMessage()));
    }

    @Test
    void verifyEventForUpdateByAdminStateException() {
        Long userId = 1L;
        Long eventId = 1L;
        eventService.addEvent(userId, newEventDto1);
        Event event = repository.findById(eventId).orElseThrow();
        event.setState(EventState.PUBLISHED);
        repository.saveAndFlush(event);

        final ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.updateEventByAdmin(eventId, updateEventAdmin));

        assertThat("Cannot publish the event because of the wrong current state", is(exception.getMessage()));
    }

    @Test
    void searchForEventsByAdminWithParametersTest() {
        Integer expectedSize = 2;
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long categoryId1 = 1L;
        Long categoryId3 = 3L;
        Long eventId2 = 2L;
        Long eventId5 = 5L;
        PageRequest pageRequest = PageRequest.of(0, 10);
        eventService.addEvent(userId1, newEventDto1);
        eventService.addEvent(userId1, newEventDto2);
        Event event2 = repository.findById(eventId2).orElseThrow();
        event2.setState(EventState.CANCELED);
        repository.saveAndFlush(event2);
        eventService.addEvent(userId1, newEventDto3);
        eventService.addEvent(userId2, newEventDto4);
        eventService.addEvent(userId2, newEventDto5);
        Event event5 = repository.findById(eventId5).orElseThrow();
        event5.setState(EventState.PUBLISHED);
        repository.saveAndFlush(event5);
        eventService.addEvent(userId2, newEventDto6);
        List<Long> users = List.of(userId1, userId2);
        List<EventState> states = List.of(EventState.CANCELED, EventState.PUBLISHED);
        List<Long> categories = List.of(categoryId1, categoryId3);
        String start = "2022-01-06%2013%3A30%3A38";
        String end = "2097-09-06%2013%3A30%3A38";

        List<EventFullDto> events = eventService.searchForEventsByAdmin(users, states, categories, start, end, pageRequest);
        EventFullDto eventFromRepository1 = events.get(0);
        EventFullDto eventFromRepository2 = events.get(1);

        assertThat(events.size(), is(expectedSize));
        assertThat(newEventDto2.getAnnotation(), is(eventFromRepository1.getAnnotation()));
        assertThat(newEventDto2.getCategory(), is(eventFromRepository1.getCategory().getId()));
        assertThat(newEventDto2.getEventDate(), is(eventFromRepository1.getEventDate()));
        assertThat(newEventDto2.getTitle(), is(eventFromRepository1.getTitle()));
        assertThat(newEventDto5.getAnnotation(), is(eventFromRepository2.getAnnotation()));
        assertThat(newEventDto5.getCategory(), is(eventFromRepository2.getCategory().getId()));
        assertThat(newEventDto5.getEventDate(), is(eventFromRepository2.getEventDate()));
        assertThat(newEventDto5.getTitle(), is(eventFromRepository2.getTitle()));
    }

    @Test
    void searchForEventsByAdminWithoutParametersTest() {
        Integer expectedSize = 6;
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long userId3 = 3L;
        PageRequest pageRequest = PageRequest.of(0, 10);
        eventService.addEvent(userId1, newEventDto1);
        eventService.addEvent(userId1, newEventDto2);
        eventService.addEvent(userId1, newEventDto3);
        eventService.addEvent(userId2, newEventDto4);
        eventService.addEvent(userId2, newEventDto5);
        eventService.addEvent(userId3, newEventDto6);
        List<Long> users = null;
        List<EventState> states = null;
        List<Long> categories = null;
        String start = null;
        String end = null;

        List<EventFullDto> events = eventService.searchForEventsByAdmin(users, states, categories, start, end, pageRequest);
        EventFullDto eventFromRepository1 = events.get(0);
        EventFullDto eventFromRepository2 = events.get(1);
        EventFullDto eventFromRepository3 = events.get(2);
        EventFullDto eventFromRepository4 = events.get(3);
        EventFullDto eventFromRepository5 = events.get(4);
        EventFullDto eventFromRepository6 = events.get(5);

        assertThat(events.size(), is(expectedSize));
        assertThat(newEventDto1.getAnnotation(), is(eventFromRepository1.getAnnotation()));
        assertThat(newEventDto1.getCategory(), is(eventFromRepository1.getCategory().getId()));
        assertThat(newEventDto2.getAnnotation(), is(eventFromRepository2.getAnnotation()));
        assertThat(newEventDto2.getCategory(), is(eventFromRepository2.getCategory().getId()));
        assertThat(newEventDto3.getAnnotation(), is(eventFromRepository3.getAnnotation()));
        assertThat(newEventDto3.getCategory(), is(eventFromRepository3.getCategory().getId()));
        assertThat(newEventDto4.getAnnotation(), is(eventFromRepository4.getAnnotation()));
        assertThat(newEventDto4.getCategory(), is(eventFromRepository4.getCategory().getId()));
        assertThat(newEventDto5.getAnnotation(), is(eventFromRepository5.getAnnotation()));
        assertThat(newEventDto5.getCategory(), is(eventFromRepository5.getCategory().getId()));
        assertThat(newEventDto6.getAnnotation(), is(eventFromRepository6.getAnnotation()));
        assertThat(newEventDto6.getCategory(), is(eventFromRepository6.getCategory().getId()));
    }

    private UpdateEventAdmin makeUpdateEventAdmin(String title, String annotation) {
        UpdateEventAdmin.UpdateEventAdminBuilder builder = UpdateEventAdmin.builder();

        builder.annotation(annotation);
        builder.eventDate(LocalDateTime.now().plusDays(1));
        builder.paid(true);
        builder.requestModeration(false);
        builder.title(title);

        return builder.build();
    }

    private UpdateEvent makeUpdateEvent(String title, String annotation) {
        UpdateEvent.UpdateEventBuilder builder = UpdateEvent.builder();

        builder.annotation(annotation);
        builder.eventDate(LocalDateTime.now().plusDays(5));
        builder.paid(false);
        builder.title(title);

        return builder.build();
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto.UserDtoBuilder builder = UserDto.builder();

        builder.name(name);
        builder.email(email);

        return builder.build();
    }

    private NewCategoryDto makeCategory(String name) {
        NewCategoryDto.NewCategoryDtoBuilder builder = NewCategoryDto.builder();

        builder.name(name);

        return builder.build();
    }

    private NewEventDto makeNewEventDto(String title, Long category) {
        NewEventDto.NewEventDtoBuilder builder = NewEventDto.builder();

        builder.annotation("annotation");
        builder.description("description");
        builder.eventDate(LocalDateTime.now().plusDays(1));
        builder.location(LocationDto.builder().latitude(38.3234F).longitude(84.342F).build());
        builder.category(category);
        builder.paid(true);
        builder.participantLimit(0L);
        builder.requestModeration(false);
        builder.title(title);

        return builder.build();
    }

    private void resetIdColumns() {
        entityManager
                .createNativeQuery("ALTER TABLE events ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
