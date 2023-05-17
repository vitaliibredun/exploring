package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comment.constants.CommentStatus;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentInfo;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventService eventService;
    private final UserService userService;
    private final RequestService requestService;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userService.getUser(userId);
        Event event = doEventValidation(userId, eventId);
        Comment comment = commentMapper.toModel(event, user, newCommentDto);
        comment.setCreated(LocalDateTime.now());
        comment.setStatus(CommentStatus.WAITING);
        Comment commentFromRepository = commentRepository.save(comment);
        return commentMapper.toDto(commentFromRepository);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = checkIfCommentExist(commentId);
        checkIfOwnerOfComment(userId, comment);
        doCommentValidation(userId, comment);
        comment.setText(updateCommentDto.getText());
        Comment commentFromRepository = commentRepository.saveAndFlush(comment);
        return commentMapper.toDto(commentFromRepository);
    }

    @Override
    public CommentDto getComment(Long userId, Long commentId) {
        Comment comment = checkIfCommentExist(commentId);
        checkIfOwnerOfComment(userId, comment);
        return commentMapper.toDto(comment);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = checkIfCommentExist(commentId);
        checkIfOwnerOfComment(userId, comment);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentInfo> getCommentsToEvent(Long eventId, Pageable pageable) {
        Event event = eventService.findEventById(eventId);
        checkIfEventFinished(event);
        return commentRepository.findCommentsToEvent(eventId, pageable)
                .stream()
                .map(commentMapper::toDtoInfo)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto approvingComment(Long commentId, CommentStatus status) {
        Comment comment = checkIfCommentExist(commentId);

        switch (status) {
            case APPROVED:
                comment.setStatus(CommentStatus.APPROVED);
                commentRepository.saveAndFlush(comment);
                return commentMapper.toDto(comment);

            case REJECTED:
                comment.setStatus(CommentStatus.REJECTED);
                commentRepository.saveAndFlush(comment);
                return commentMapper.toDto(comment);

            default:
                log.error("There is no such status");
                throw new BadRequestException("There is no such status");
        }
    }

    @Override
    public CommentDto getCommentByAdmin(Long commentId) {
        Comment comment = checkIfCommentExist(commentId);
        return commentMapper.toDto(comment);
    }

    private void doCommentValidation(Long userId, Comment comment) {
        userService.getUser(userId);

        boolean statusApproved = comment.getStatus() == CommentStatus.APPROVED;
        if (statusApproved) {
            log.error("The status of comment already approved");
            throw new ConflictException("The status of comment already approved");
        }

        boolean statusRejected = comment.getStatus() == CommentStatus.REJECTED;
        if (statusRejected) {
            log.error("The status of comment already rejected");
            throw new ConflictException("The status of comment already rejected");
        }
    }

    private Event doEventValidation(Long userId, Long eventId) {
        Event event = eventService.findEventById(eventId);
        boolean eventInFuture = event.getEventDate().isAfter(LocalDateTime.now());
        if (eventInFuture) {
            log.error("The event not finished yet");
            throw new BadRequestException("The event not finished yet");
        }

        Optional<Request> request = requestService.findRequest(userId, eventId);
        if (request.isEmpty()) {
            log.error("The user was not in event");
            throw new NotExistsException("The user was not in event");
        }

        return event;
    }

    private Comment checkIfCommentExist(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            log.error("Comment with id = {} was not found", commentId);
            throw new NotExistsException("Comment was not found");
        }

        return comment.get();
    }

    private void checkIfOwnerOfComment(Long userId, Comment comment) {
        boolean notOwnerOfComment = !Objects.equals(comment.getAuthor().getId(), userId);
        if (notOwnerOfComment) {
            log.error("Not owner of comment with id = {}", comment.getId());
            throw new BadRequestException("Not owner of comment");
        }
    }

    private void checkIfEventFinished(Event event) {
        boolean eventInFuture = event.getEventDate().isAfter(LocalDateTime.now());
        if (eventInFuture) {
            log.error("The event not finished yet");
            throw new BadRequestException("The event not finished yet");
        }
    }
}
