package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.admin.dto.UpdateEventAdmin;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.constants.Sort;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent updateEvent);

    EventFullDto getEvent(Long userId, Long eventId);

    List<EventShortDto> getAllEventsByUser(Long userId, Pageable pageable);

    Event getEventById(Long eventId);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdmin updateEvent);

    List<EventFullDto> searchForEventsByAdmin(List<Long> users,
                                              List<EventState> states,
                                              List<Long> categories,
                                              String rangeStart,
                                              String rangeEnd,
                                              Pageable pageable);

    List<Event> getAllEventsByIds(Set<Long> eventsIds);

    List<EventShortDto> searchForEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        Boolean onlyAvailable,
                                        Sort sort,
                                        Pageable pageable,
                                        HttpServletRequest httpServletRequest);

    EventFullDto getEvent(Long id, HttpServletRequest httpServletRequest);
}
