package ru.practicum.ewm.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.location.model.Location;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {

    Location toModel(LocationDto locationDto);

    LocationDto toDto(Location location);
}
