package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.admin.constants.StateAction;
import ru.practicum.ewm.admin.dto.UpdateEventAdmin;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.constants.EventStateAction;
import ru.practicum.ewm.event.constants.Sort;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        doValidation(newEventDto);
        Category category = checkCategoryExist(newEventDto.getCategory());
        Location location = mapAndSaveLocation(newEventDto.getLocation());
        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventMapper.toModel(newEventDto, category, location, user);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        Event eventFromRepository = eventRepository.save(event);
        return eventMapper.toDto(eventFromRepository);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent updateEvent) {
        doValidation(updateEvent);
        Event event = doUpdateEvent(userId, eventId, updateEvent);
        Event eventFromRepository = eventRepository.saveAndFlush(event);
        return eventMapper.toDto(eventFromRepository);
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        Event event = checkEventExist(eventId);
        checkOwnerOfEvent(userId, event);
        return eventMapper.toDto(event);
    }

    @Override
    public List<EventShortDto> getAllEventsByUser(Long userId, Pageable pageable) {
        return eventRepository.findAllByUserId(userId, pageable)
                .stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public Event getEventById(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            log.error("Event with id = {} was not found", event);
            throw new NotExistsException("Event was not found");
        }
        return event.get();
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdmin updateEvent) {
        Event event = doUpdateEvent(eventId, updateEvent);
        Event eventFromRepository = eventRepository.saveAndFlush(event);
        return eventMapper.toDto(eventFromRepository);
    }

    @Override
    public List<EventFullDto> searchForEventsByAdmin(List<Long> users,
                                                     List<EventState> states,
                                                     List<Long> categories,
                                                     String rangeStart,
                                                     String rangeEnd,
                                                     Pageable pageable) {

        LocalDateTime start = decodeTime(rangeStart);
        LocalDateTime end = decodeTime(rangeEnd);

        return eventRepository.searchEventsByAdmin(users, states, categories, start, end, pageable)
                .stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getAllEventsByIds(Set<Long> eventsIds) {
        eventsIds.forEach(this::checkEventExist);
        return eventRepository.findAllById(eventsIds);
    }

    @Override
    public List<EventShortDto> searchForEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               Sort sort,
                                               Pageable pageable,
                                               HttpServletRequest httpServletRequest) {

        LocalDateTime start = decodeTime(rangeStart);
        LocalDateTime end = decodeTime(rangeEnd);
        List<Event> events = eventRepository.searchEventsByUser(text, categories, paid, start, end, pageable);
        List<Event> eventsWitViews = addViews(events);
        List<Event> eventsWithConfirmedRequests = addConfirmedRequest(eventsWitViews);
        List<Event> checkedEvents = checkEventsAvailableLimit(onlyAvailable, eventsWithConfirmedRequests);
        checkedEvents.forEach(eventRepository::saveAndFlush);
        saveStats(httpServletRequest);

        if (sort != null) {
            switch (sort) {
                case EVENT_DATE:
                    return checkedEvents
                            .stream()
                            .sorted(Comparator.comparing(Event::getEventDate).reversed())
                            .map(eventMapper::toShortDto)
                            .collect(Collectors.toList());
                case VIEWS:
                    return checkedEvents
                            .stream()
                            .sorted(Comparator.comparing(Event::getViews))
                            .map(eventMapper::toShortDto)
                            .collect(Collectors.toList());
            }
        }

        return checkedEvents
                .stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest httpServletRequest) {
        Event event = checkEvent(id);
        Event eventWitViews = addViews(event);
        Event eventsWithConfirmedRequests = addConfirmedRequest(eventWitViews);
        eventRepository.saveAndFlush(eventsWithConfirmedRequests);
        saveStats(httpServletRequest);
        return eventMapper.toDto(eventsWithConfirmedRequests);
    }

    private Event checkEvent(Long id) {
        Optional<Event> event = Optional.ofNullable(eventRepository.findEvent(id));
        if (event.isEmpty()) {
            log.error("Event with id = {} was not found", event);
            throw new NotExistsException("Event with id was not found");
        }
        return event.get();

    }

    private void saveStats(HttpServletRequest httpServletRequest) {
        String app = "main-service";
        EndpointHit.EndpointHitBuilder builder = EndpointHit.builder();

        builder.app(app);
        builder.uri(httpServletRequest.getRequestURI());
        builder.ip(httpServletRequest.getRemoteAddr());
        builder.timestamp(LocalDateTime.now());

        EndpointHit endPoint = builder.build();

        statsClient.saveStats(endPoint);
    }

    private List<Event> checkEventsAvailableLimit(Boolean onlyAvailable, List<Event> events) {
        if (onlyAvailable) {
            return events
                    .stream()
                    .filter(event -> event.getParticipantLimit() < event.getConfirmedRequests())
                    .collect(Collectors.toList());
        }
        return events;
    }

    private Event addConfirmedRequest(Event event) {
        Long allConfirmed = requestRepository.findQuantityAllConfirmed(event.getId());
        event.setConfirmedRequests(allConfirmed);
        return event;
    }

    private List<Event> addConfirmedRequest(List<Event> events) {
        List<Long> confirmedRequests = new ArrayList<>();

        for (Event event : events) {
            Long eventId = event.getId();
            Long confirmed = requestRepository.findQuantityAllConfirmed(eventId);
            confirmedRequests.add(confirmed);
        }

        for (int i = 0; i < events.size(); i++) {
            events.get(i).setConfirmedRequests(confirmedRequests.get(i));
        }
        return events;
    }

    private Event addViews(Event event) {
        Long oneView = 1L;

        if (event.getViews() == null) {
            event.setViews(oneView);
        } else {
            event.setViews(event.getViews() + oneView);
        }
        return event;
    }

    private List<Event> addViews(List<Event> events) {
        Long oneView = 1L;

        events.forEach(event -> {
            if (event.getViews() == null) {
                event.setViews(oneView);
            } else {
                event.setViews(event.getViews() + oneView);
            }
        });
        return events;
    }

    private LocalDateTime decodeTime(String time) {
        if (time == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(URLDecoder.decode(time, StandardCharsets.UTF_8), formatter);
    }

    private void validateEvent(Event event, UpdateEventAdmin updateEvent) {
        boolean wantToReject = updateEvent.getStateAction() == StateAction.REJECT_EVENT;
        boolean alreadyPublished = event.getState() == EventState.PUBLISHED;
        boolean lessThanNeed = event.getEventDate().isBefore(LocalDateTime.now().plusHours(1));
        boolean eventNoPending = event.getState() != EventState.PENDING;

        boolean timeInPast = updateEvent.getEventDate() != null
                && updateEvent.getEventDate().isBefore(LocalDateTime.now());
        if (timeInPast) {
            log.error("Error: Time of event cannot be in the past");
            throw new ConflictException("Time of event cannot be in the past");
        }

        if (wantToReject && alreadyPublished) {
            log.error("Error: Cannot reject the event because it is published");
            throw new ConflictException("Cannot reject the event because it had been published");
        }
        if (lessThanNeed) {
            log.error("Field: eventDate. Error: eventDate is less than 1 hour before publication time");
            throw new ConflictException("EventDate of the event is less than 1 hour before publication time");
        }
        if (eventNoPending) {
            log.error("Field: eventDate. Error: The wrong current state = {}", event.getState());
            throw new ConflictException("Cannot publish the event because of the wrong current state");
        }
    }

    private Event checkEventExist(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            log.error("Event with id = {} was not found", event);
            throw new NotExistsException("Event with id was not found");
        }
        return event.get();
    }

    private void checkOwnerOfEvent(Long userId, Event event) {
        boolean notOwner = !Objects.equals(event.getInitiator().getId(), userId);
        if (notOwner) {
            log.error("Field: initiator. Error: only owner of event can makes changes");
            throw new ConflictException("Only owner of event can makes changes");
        }
    }

    private Event doUpdateEvent(Long userId, Long eventId, UpdateEvent updateEvent) {
        Event event = checkEvent(userId, eventId);

        boolean newAnnotation = updateEvent.getAnnotation() != null;
        boolean newCategory = updateEvent.getCategory() != null;
        boolean newDescription = updateEvent.getDescription() != null;
        boolean newEventDate = updateEvent.getEventDate() != null;
        boolean newLocation = updateEvent.getLocation() != null;
        boolean newPaid = updateEvent.getPaid() != null;
        boolean newParticipantLimit = updateEvent.getParticipantLimit() != null;
        boolean newRequestModeration = updateEvent.getRequestModeration() != null;
        boolean newStateAction = updateEvent.getStateAction() != null;
        boolean newTitle = updateEvent.getTitle() != null;

        if (newAnnotation) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (newCategory) {
            Category category = checkCategoryExist(updateEvent.getCategory());
            event.setCategory(category);
        }
        if (newDescription) {
            event.setDescription(updateEvent.getDescription());
        }
        if (newEventDate) {
            event.setEventDate(updateEvent.getEventDate());
        }
        if (newLocation) {
            Location location = mapAndSaveLocation(updateEvent.getLocation());
            event.setLocation(location);
        }
        if (newPaid) {
            event.setPaid(updateEvent.getPaid());
        }
        if (newParticipantLimit) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (newRequestModeration) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (newStateAction) {
            if (updateEvent.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
            if (updateEvent.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (newTitle) {
            event.setTitle(updateEvent.getTitle());
        }
        return event;
    }

    private Event doUpdateEvent(Long eventId, UpdateEventAdmin updateEvent) {
        Event event = checkEventExist(eventId);
        validateEvent(event, updateEvent);

        boolean newAnnotation = updateEvent.getAnnotation() != null;
        boolean newCategory = updateEvent.getCategory() != null;
        boolean newDescription = updateEvent.getDescription() != null;
        boolean newEventDate = updateEvent.getEventDate() != null;
        boolean newLocation = updateEvent.getLocation() != null;
        boolean newPaid = updateEvent.getPaid() != null;
        boolean newParticipantLimit = updateEvent.getParticipantLimit() != null;
        boolean newRequestModeration = updateEvent.getRequestModeration() != null;
        boolean newStateAction = updateEvent.getStateAction() != null;
        boolean newTitle = updateEvent.getTitle() != null;

        if (newAnnotation) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (newCategory) {
            Category category = checkCategoryExist(updateEvent.getCategory());
            event.setCategory(category);
        }
        if (newDescription) {
            event.setDescription(updateEvent.getDescription());
        }
        if (newEventDate) {
            event.setEventDate(updateEvent.getEventDate());
        }
        if (newLocation) {
            Location location = mapAndSaveLocation(updateEvent.getLocation());
            event.setLocation(location);
        }
        if (newPaid) {
            event.setPaid(updateEvent.getPaid());
        }
        if (newParticipantLimit) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (newRequestModeration) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (newStateAction) {
            if (updateEvent.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEvent.getStateAction().equals(StateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (newTitle) {
            event.setTitle(updateEvent.getTitle());
        }
        return event;
    }

    private Event checkEvent(Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            log.error("Event with id = {} was not found", event);
            throw new NotExistsException("Event with id was not found");
        }
        boolean notOwner = !Objects.equals(event.get().getInitiator().getId(), userId);
        if (notOwner) {
            log.error("Field: initiator. Error: only owner of event can makes changes");
            throw new ConflictException("Only owner of event can makes changes");

        }
        boolean eventIsPublished = event.get().getState() == EventState.PUBLISHED;
        if (eventIsPublished) {
            log.error("Field: state. Error: only pending or canceled events can be changed");
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        return event.get();
    }

    private Category checkCategoryExist(Long catId) {
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isEmpty()) {
            log.error("Field: category. Error: the category with id {} doesn't exist", catId);
            throw new NotExistsException("The required object was not found.");
        }
        return category.get();
    }

    private void doValidation(NewEventDto eventDto) {
        boolean annotationIsNull = eventDto.getAnnotation() == null;
        boolean categoryIsNull = eventDto.getCategory() == null;
        boolean descriptionIsNull = eventDto.getDescription() == null;
        boolean eventDateIsNull = eventDto.getEventDate() == null;
        boolean locationIsNull = eventDto.getLocation() == null;
        boolean paidIsNull = eventDto.getPaid() == null;
        boolean participantLimitIsNull = eventDto.getParticipantLimit() == null;
        boolean requestModerationIsNull = eventDto.getRequestModeration() == null;
        boolean titleIsNull = eventDto.getTitle() == null;

        if (annotationIsNull || categoryIsNull || descriptionIsNull || eventDateIsNull
                || locationIsNull || paidIsNull || participantLimitIsNull || requestModerationIsNull || titleIsNull) {

            log.error("Error: the fields of event must not be null");
            throw new BadRequestException("The fields of event must not be null");
        }

        boolean eventDateLessThenAnHour = eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2));
        if (eventDateLessThenAnHour) {
            log.error("Field: eventDate. Error: The time of event less then 2 hours from current time");
            throw new ConflictException("The time of event less then 2 hours from current time");
        }
    }

    private void doValidation(UpdateEvent updateEvent) {
        boolean timeEventLessThenTwoHours = updateEvent.getEventDate() != null && updateEvent.getEventDate()
                .isBefore(LocalDateTime.now().plusHours(2));
        if (timeEventLessThenTwoHours) {
            log.error("Field: eventDate. Error: The time of event less then 2 hours from current time");
            throw new ConflictException("The time of event less then 2 hours from current time");
        }
    }

    private Location mapAndSaveLocation(LocationDto locationDto) {
        Location location = locationMapper.toModel(locationDto);
        return locationRepository.save(location);
    }
}
