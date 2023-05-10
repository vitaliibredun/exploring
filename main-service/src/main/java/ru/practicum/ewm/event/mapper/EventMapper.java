package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

@Mapper
public interface EventMapper {

    default Event toModel(NewEventDto newEventDto, Category category, Location location, User user) {
        if (newEventDto == null) {
            return null;
        }

        Event.EventBuilder builder = Event.builder();

        builder.annotation(newEventDto.getAnnotation());
        builder.category(category);
        builder.description(newEventDto.getDescription());
        builder.eventDate(newEventDto.getEventDate());
        builder.initiator(user);
        builder.location(location);
        builder.paid(newEventDto.getPaid());
        builder.participantLimit(newEventDto.getParticipantLimit());
        builder.requestModeration(newEventDto.getRequestModeration());
        builder.title(newEventDto.getTitle());

        return builder.build();
    }

    default EventFullDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        EventFullDto.EventFullDtoBuilder builder = EventFullDto.builder();

        builder.id(event.getId());
        builder.annotation(event.getAnnotation());
        builder.confirmedRequests(event.getConfirmedRequests());
        builder.category(CategoryDto
                .builder().id(event.getCategory().getId()).name(event.getCategory().getName()).build());
        builder.createdOn(event.getCreatedOn());
        builder.description(event.getDescription());
        builder.eventDate(event.getEventDate());
        builder.initiator(UserShortDto
                .builder().id(event.getInitiator().getId()).name(event.getInitiator().getName()).build());
        builder.location(LocationDto
                .builder().latitude(event.getLocation().getLatitude()).longitude(event.getLocation().getLongitude()).build());
        builder.paid(event.getPaid());
        builder.participantLimit(event.getParticipantLimit());
        builder.publishedOn(event.getPublishedOn());
        builder.requestModeration(event.getRequestModeration());
        builder.state(event.getState());
        builder.title(event.getTitle());
        builder.views(event.getViews());

        return builder.build();
    }

    default EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        EventShortDto.EventShortDtoBuilder builder = EventShortDto.builder();

        builder.id(event.getId());
        builder.annotation(event.getAnnotation());
        builder.confirmedRequests(event.getConfirmedRequests());
        builder.category(CategoryDto.builder().id(event.getCategory().getId()).name(event.getCategory().getName()).build());
        builder.eventDate(event.getEventDate());
        builder.initiator(UserShortDto.builder().id(event.getInitiator().getId()).name(event.getInitiator().getName()).build());
        builder.paid(event.getPaid());
        builder.title(event.getTitle());
        builder.views(event.getViews());

        return builder.build();
    }
}
