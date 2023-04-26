package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.model.Stat;

@Mapper
public interface StatsMapper {

    Stat toModel(EndpointHit endpointHit);

    EndpointHit toDto(Stat stat);
}
