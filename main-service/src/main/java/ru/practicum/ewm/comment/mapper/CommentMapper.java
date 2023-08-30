package ru.practicum.ewm.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentInfo;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    Comment toModel(Event event, User user, NewCommentDto newCommentDto);

    @Mapping(target = "event", source = "comment",qualifiedByName = "createEvent")
    @Mapping(target = "author", source = "comment",qualifiedByName = "createAuthor")
    CommentDto toDto(Comment comment);

    @Mapping(target = "event", source = "comment",qualifiedByName = "createEvent")
    @Mapping(target = "author", source = "comment",qualifiedByName = "createAuthor")
    CommentInfo toDtoInfo(Comment comment);

    @Named("createEvent")
    default EventShortDto createEvent(Comment comment) {
        return EventShortDto
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
                .build();
    }

    @Named("createAuthor")
    default UserShortDto createAuthor(Comment comment) {
        return UserShortDto.builder().id(comment.getAuthor().getId())
                .name(comment.getAuthor().getName()).build();
    }
}
