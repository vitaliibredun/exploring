package ru.practicum.ewm.comment.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentInfo;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

@Mapper
public interface CommentMapper {

    default Comment toModel(Event event, User user, NewCommentDto newCommentDto) {
        if (event == null || user == null || newCommentDto == null) {
            return null;
        }

        Comment.CommentBuilder builder = Comment.builder();

        builder.text(newCommentDto.getText());
        builder.event(event);
        builder.author(user);

        return builder.build();
    }

    default CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDto.CommentDtoBuilder builder = CommentDto.builder();

        builder.id(comment.getId());
        builder.text(comment.getText());
        builder.event(EventShortDto
                .builder()
                .id(comment.getEvent().getId())
                .annotation(comment.getEvent().getAnnotation())
                .category(CategoryDto.builder().id(comment.getEvent().getCategory().getId())
                        .name(comment.getEvent().getCategory().getName()).build())
                .eventDate(comment.getEvent().getEventDate())
                .initiator(UserShortDto.builder().id(comment.getEvent().getInitiator().getId())
                        .name(comment.getEvent().getInitiator().getName()).build())
                .paid(comment.getEvent().getPaid())
                .title(comment.getEvent().getTitle())
                .views(comment.getEvent().getViews())
                .build());
        builder.author(UserShortDto.builder().id(comment.getAuthor().getId())
                .name(comment.getAuthor().getName()).build());
        builder.status(comment.getStatus());

        return builder.build();
    }

    default CommentInfo toDtoInfo(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentInfo.CommentInfoBuilder builder = CommentInfo.builder();

        builder.id(comment.getId());
        builder.text(comment.getText());
        builder.event(EventShortDto
                .builder()
                .id(comment.getEvent().getId())
                .annotation(comment.getEvent().getAnnotation())
                .category(CategoryDto.builder().id(comment.getEvent().getCategory().getId())
                        .name(comment.getEvent().getCategory().getName()).build())
                .eventDate(comment.getEvent().getEventDate())
                .initiator(UserShortDto.builder().id(comment.getEvent().getInitiator().getId())
                        .name(comment.getEvent().getInitiator().getName()).build())
                .paid(comment.getEvent().getPaid())
                .title(comment.getEvent().getTitle())
                .views(comment.getEvent().getViews())
                .build());
        builder.author(UserShortDto.builder().id(comment.getAuthor().getId())
                .name(comment.getAuthor().getName()).build());
        builder.created(comment.getCreated());

        return builder.build();
    }
}
