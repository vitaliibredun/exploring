package ru.practicum.ewm.request.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestResult {
    private List<RequestToEvent> confirmedRequests;
    private List<RequestToEvent> rejectedRequests;
}
