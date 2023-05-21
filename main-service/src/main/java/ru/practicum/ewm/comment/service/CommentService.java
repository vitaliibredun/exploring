package ru.practicum.ewm.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.comment.constants.CommentStatus;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentInfo;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    CommentDto getComment(Long userId, Long commentId);

    void deleteComment(Long userId, Long commentId);

    List<CommentInfo> getCommentsToEvent(Long eventId, Pageable pageable);

    CommentDto approvingComment(Long commentId, CommentStatus status);

    CommentDto getCommentByAdmin(Long commentId);
}
