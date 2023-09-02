package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
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

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "location", source = "location")
    Event toModel(NewEventDto newEventDto, Category category, Location location, User user);

    @Mapping(target = "category", source = "event", qualifiedByName = "createCategory")
    @Mapping(target = "initiator", source = "event", qualifiedByName = "createInitiator")
    @Mapping(target = "location", source = "event", qualifiedByName = "createLocation")
    EventFullDto toDto(Event event);

    @Mapping(target = "category", source = "event", qualifiedByName = "createCategory")
    @Mapping(target = "initiator", source = "event", qualifiedByName = "createInitiator")
    EventShortDto toShortDto(Event event);

    @Named("createCategory")
    default CategoryDto createCategory(Event event) {
        return CategoryDto
                .builder().id(event.getCategory().getId()).name(event.getCategory().getName()).build();
    }

    @Named("createInitiator")
    default UserShortDto createInitiator(Event event) {
        return UserShortDto
                .builder().id(event.getInitiator().getId()).name(event.getInitiator().getName()).build();
    }

    @Named("createLocation")
    default LocationDto createLocation(Event event) {
        return LocationDto
                .builder().lat(event.getLocation().getLat()).lon(event.getLocation().getLon()).build();
    }
}
