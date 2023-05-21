package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.request.constants.RequestState;
import ru.practicum.ewm.request.dto.RequestToEvent;
import ru.practicum.ewm.request.dto.EventRequestResult;
import ru.practicum.ewm.request.dto.EventRequestStatus;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final EventService eventService;
    private final UserService userService;
    private final RequestMapper mapper;

    @Override
    public List<RequestToEvent> getRequestsByEvent(Long userId, Long eventId) {
        checkOwnerOfEvent(userId, eventId);
        return repository.findAllBy(userId, eventId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestResult updateStatusOfEvent(Long userId, Long eventId, EventRequestStatus requestStatus) {
        checkIfUserExist(userId);
        Event event = checkOwnerOfEvent(userId, eventId);
        List<Long> requestIds = requestStatus.getRequestIds();
        List<Request> allInputRequests = repository.findAllById(requestIds);
        checkStatusOfRequests(allInputRequests);

        if (noLimitAndModeration(event, requestStatus)) {
            List<RequestToEvent> confirmedRequests = allInputRequests
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
            List<RequestToEvent> rejectedRequests = new ArrayList<>();
            return mapper.toDto(confirmedRequests, rejectedRequests);
        }

        RequestState status = requestStatus.getStatus();
        switch (status) {
            case REJECTED:
                allInputRequests.forEach(request -> request.setStatus(RequestState.REJECTED));
                allInputRequests.forEach(repository::saveAndFlush);
                List<RequestToEvent> rejectedRequests = allInputRequests
                        .stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());

                List<RequestToEvent> confirmedRequests = new ArrayList<>();

                return mapper.toDto(confirmedRequests, rejectedRequests);

            case CONFIRMED:
                int lastForConfirm = checkIfSpaceForRequests(event);

                List<RequestToEvent> requestsConfirmed = collectConfirmedRequests(allInputRequests, lastForConfirm);
                List<RequestToEvent> requestsRejected = collectRejectedRequests(allInputRequests, lastForConfirm);

                return mapper.toDto(requestsConfirmed, requestsRejected);

            default:
                log.error("Field: status. Error: there is no such status");
                throw new NotExistsException("There is no such status");
        }
    }

    @Override
    public RequestToEvent addRequestToEvent(Long userId, Long eventId) {
        Event event = checkEvent(userId, eventId);
        User user = checkUser(userId, eventId);
        Request request = buildRequest(event, user);
        Request requestFromRepository = repository.save(request);
        return mapper.toDto(requestFromRepository);
    }

    @Override
    public RequestToEvent cancelRequestToEvent(Long userId, Long requestId) {
        Request request = checkIfRequestExist(userId, requestId);
        request.setStatus(RequestState.CANCELED);
        Request canceledRequest = repository.saveAndFlush(request);
        return mapper.toDto(canceledRequest);
    }

    @Override
    public List<RequestToEvent> getAllRequestsToEventsByUser(Long userId) {
        checkIfUserExist(userId);
        return repository.findAllRequestsByUser(userId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long findQuantityAllConfirmed(Long eventId) {
        return repository.findQuantityAllConfirmed(eventId);
    }

    @Override
    public Optional<Request> findRequest(Long userId, Long eventId) {
        return repository.findByUserAndEvent(userId, eventId);
    }


    private List<RequestToEvent> collectConfirmedRequests(List<Request> requests, Integer lastForConfirm) {
        int max = requests.size();

        if (lastForConfirm == 1) {
            requests.forEach(request -> request.setStatus(RequestState.CONFIRMED));
            requests.forEach(repository::saveAndFlush);
            return requests
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }

        if (lastForConfirm > max) {
            List<Request> requestsForConfirm = requests.subList(0, max);
            requestsForConfirm.forEach(request -> request.setStatus(RequestState.CONFIRMED));
            requestsForConfirm.forEach(repository::saveAndFlush);
            return requestsForConfirm
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }
        List<Request> requestsForConfirm = requests.subList(0, lastForConfirm);
        requestsForConfirm.forEach(request -> request.setStatus(RequestState.CONFIRMED));
        requestsForConfirm.forEach(repository::saveAndFlush);
        return requestsForConfirm
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

    }

    private List<RequestToEvent> collectRejectedRequests(List<Request> requests, Integer lastForConfirm) {
        int max = requests.size();

        if (max == 1) {
            return new ArrayList<>();
        }

        List<Request> requestsForReject = requests.subList(lastForConfirm, max);
        requestsForReject.forEach(request -> request.setStatus(RequestState.REJECTED));
        requestsForReject.forEach(repository::saveAndFlush);
        return requestsForReject
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private void checkStatusOfRequests(List<Request> requests) {
        requests.forEach(request -> {
            if (request.getStatus() != RequestState.PENDING) {
                log.error("Request must have status PENDING");
                throw new ConflictException("Request must have status PENDING");
            }
        });
    }

    private Integer checkIfSpaceForRequests(Event event) {
        Long confirmedRequests = repository.findQuantityAllConfirmed(event.getId());
        Long participantLimit = event.getParticipantLimit();

        boolean isLimitOfParticipants = confirmedRequests >= event.getParticipantLimit();
        if (isLimitOfParticipants) {
            log.error("The participant limit to event with id = {} has been reached", event.getId());
            throw new ConflictException("The participant limit has been reached");
        }

        return Math.toIntExact(participantLimit - confirmedRequests);
    }

    private boolean noLimitAndModeration(Event event, EventRequestStatus requestStatus) {
        boolean statusConfirmed = requestStatus.getStatus() == RequestState.CONFIRMED;
        boolean noLimit = event.getParticipantLimit() == 0;
        boolean noModeration = event.getRequestModeration().equals(false);
        return noLimit || noModeration && (statusConfirmed);
    }

    private Event checkOwnerOfEvent(Long userId, Long eventId) {
        Event event = eventService.findEventById(eventId);
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            log.error("The user with id = {} is not owner of the event with id = {}", userId, eventId);
            throw new ConflictException("The user is not owner of the event");
        }
        return event;
    }

    private void checkIfUserExist(Long userId) {
        Optional<User> user = Optional.ofNullable(userService.getUser(userId));
        if (user.isEmpty()) {
            log.error("The user with id = {} doesn't exist", userId);
            throw new NotExistsException("User with id was not found");
        }
    }

    private Request checkIfRequestExist(Long userId, Long requestId) {
        Optional<Request> request = repository.findById(requestId);
        if (request.isEmpty()) {
            log.error("The request with id = {} doesn't exist", requestId);
            throw new NotExistsException("The request doesn't exist");
        }
        boolean notOwnerOfRequest = !Objects.equals(request.get().getRequester().getId(), userId);
        if (notOwnerOfRequest) {
            log.error("The user with id = {} is not owner of the request", userId);
            throw new ConflictException("The user is not owner of the request");
        }
        return request.get();
    }

    private Request buildRequest(Event event, User user) {
        Request.RequestBuilder builder = Request.builder();

        builder.created(LocalDateTime.now());
        builder.event(event);
        builder.requester(user);

        boolean noModeration = event.getRequestModeration().equals(false);
        boolean noParticipantLimit = event.getParticipantLimit() == 0;
        boolean noNeedModerationOrLimit = noModeration || noParticipantLimit;

        if (noNeedModerationOrLimit) {
            builder.status(RequestState.CONFIRMED);
        } else {
            builder.status(RequestState.PENDING);
        }
        return builder.build();
    }

    private User checkUser(Long userId, Long eventId) {
        User user = userService.getUser(userId);
        Optional<Request> request = repository.findByUserAndEvent(userId, eventId);
        if (request.isPresent()) {
            log.error("The request to the event id = {} is already exist", eventId);
            throw new ConflictException("The request to the event is already exist");
        }
        return user;
    }

    private Event checkEvent(Long userId, Long eventId) {
        if (eventId == null) {
            log.error("Error: the eventId is not present");
            throw new BadRequestException("The eventId is not present");
        }

        Event event = eventService.findEventById(eventId);
        Long allConfirmed = repository.findQuantityAllConfirmed(eventId);

        Long eventLimit = event.getParticipantLimit();
        boolean isLimitOfParticipants = (allConfirmed >= eventLimit) && (eventLimit != 0);
        boolean publishedEvent = event.getState().equals(EventState.PUBLISHED);
        boolean requesterIsOwnerOfEvent = Objects.equals(event.getInitiator().getId(), userId);
        if (isLimitOfParticipants) {
            log.error("The participant limit to event with id = {} has been reached", eventId);
            throw new ConflictException("The participant limit has been reached");
        }
        if (!publishedEvent) {
            log.error("The event with id = {} is not published yet", eventId);
            throw new ConflictException("The event is not published yet");
        }
        if (requesterIsOwnerOfEvent) {
            log.error("The requester is owner of the event with id = {}", eventId);
            throw new ConflictException("The requester is owner of the event");
        }

        return event;
    }
}
