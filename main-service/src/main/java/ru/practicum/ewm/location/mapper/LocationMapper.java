package ru.practicum.ewm.location.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.location.dto.LocationDto;
import ru.practicum.ewm.location.model.Location;

@Mapper
public interface LocationMapper {
    default Location toModel(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        Location.LocationBuilder builder = Location.builder();

        builder.lat(locationDto.getLat());
        builder.lon(locationDto.getLon());

        return builder.build();
    }

    default LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDto.LocationDtoBuilder builder = LocationDto.builder();

        builder.lat(location.getLat());
        builder.lon(location.getLon());

        return builder.build();
    }
}
