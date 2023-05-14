package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.request.dto.EventRequestResult;
import ru.practicum.ewm.request.dto.RequestToEvent;
import ru.practicum.ewm.request.model.Request;

import java.util.List;

@Mapper
public interface RequestMapper {
    default RequestToEvent toDto(Request request) {
        if (request == null) {
            return null;
        }

        RequestToEvent.RequestToEventBuilder builder = RequestToEvent.builder();

        builder.id(request.getId());
        builder.created(request.getCreated());
        builder.event(request.getEvent().getId());
        builder.requester(request.getRequester().getId());
        builder.status(request.getStatus());

        return builder.build();
    }

    default EventRequestResult toDto(List<RequestToEvent> confirmedRequests, List<RequestToEvent> rejectedRequests) {

        EventRequestResult.EventRequestResultBuilder builder = EventRequestResult.builder();

        builder.confirmedRequests(confirmedRequests);
        builder.rejectedRequests(rejectedRequests);

        return builder.build();
    }
}
