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

        builder.latitude(locationDto.getLatitude());
        builder.longitude(locationDto.getLongitude());

        return builder.build();
    }

    default LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDto.LocationDtoBuilder builder = LocationDto.builder();

        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());

        return builder.build();
    }
}
