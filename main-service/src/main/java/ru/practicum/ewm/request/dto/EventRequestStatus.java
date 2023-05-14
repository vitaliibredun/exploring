package ru.practicum.ewm.request.dto;

import lombok.*;
import ru.practicum.ewm.request.constants.RequestState;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatus {
    private List<Long> requestIds;
    private RequestState status;
}
