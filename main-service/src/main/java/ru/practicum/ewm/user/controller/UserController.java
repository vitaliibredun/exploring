package ru.practicum.ewm.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentInfo;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestResult;
import ru.practicum.ewm.request.dto.EventRequestStatus;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.request.dto.RequestToEvent;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto eventDto) {
        return eventService.addEvent(userId, eventDto);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEvent updateEvent) {

        return eventService.updateEvent(userId, eventId, updateEvent);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {

        return eventService.getEvent(userId, eventId);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllEventsByUser(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        return eventService.getAllEventsByUser(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestToEvent> getRequestsByEvent(@PathVariable Long userId,
                                                   @PathVariable Long eventId) {

        return requestService.getRequestsByEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestResult updateStatusOfEvent(@PathVariable Long userId,
                                                  @PathVariable Long eventId,
                                                  @RequestBody EventRequestStatus requestStatus) {

        return requestService.updateStatusOfEvent(userId, eventId, requestStatus);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestToEvent addRequestToEvent(@PathVariable Long userId, @RequestParam(required = false) Long eventId) {
        return requestService.addRequestToEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestToEvent cancelRequestToEvent(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequestToEvent(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestToEvent> getAllRequestsToEventsByUser(@PathVariable Long userId) {
        return requestService.getAllRequestsToEventsByUser(userId);
    }

    @PostMapping("/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Valid @RequestBody NewCommentDto newCommentDto) {

        return commentService.addComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Long userId,
                                 @PathVariable Long commentId,
                                 @Valid @RequestBody UpdateCommentDto updateCommentDto) {

        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @GetMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getComment(@PathVariable Long userId,
                                 @PathVariable Long commentId) {

        return commentService.getComment(userId, commentId);
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/comments/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentInfo> getCommentsToEvent(
            @PathVariable Long eventId,
            @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        return commentService.getCommentsToEvent(eventId, PageRequest.of(from / size, size));
    }
}
