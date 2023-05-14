package ru.practicum.ewm.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.admin.constants.StateAction;
import ru.practicum.ewm.location.dto.LocationDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdmin {
    private String annotation;
    private Long category;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
    private String title;
}
