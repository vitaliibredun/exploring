package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.RequestToEvent;
import ru.practicum.ewm.request.dto.EventRequestResult;
import ru.practicum.ewm.request.dto.EventRequestStatus;
import ru.practicum.ewm.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestService {

    List<RequestToEvent> getRequestsByEvent(Long userId, Long eventId);

    EventRequestResult updateStatusOfEvent(Long userId, Long eventId, EventRequestStatus requestStatus);

    RequestToEvent addRequestToEvent(Long userId, Long eventId);

    RequestToEvent cancelRequestToEvent(Long userId, Long requestId);

    List<RequestToEvent> getAllRequestsToEventsByUser(Long userId);

    Long findQuantityAllConfirmed(Long eventId);

    Optional<Request> findRequest(Long userId, Long eventId);
}
