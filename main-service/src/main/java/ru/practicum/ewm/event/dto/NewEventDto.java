package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.location.dto.LocationDto;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotEmpty(message = "The annotation field is empty")
    private String annotation;
    @NotNull(message = "The category field is empty")
    private Long category;
    @NotEmpty(message = "The description field is empty")
    private String description;
    @NotEmpty(message = "The eventDate field is empty")
    @FutureOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    @NotNull(message = "The paid field is empty")
    private Boolean paid;
    @NotNull(message = "The participantLimit field is empty")
    private Long participantLimit;
    @NotNull(message = "The requestModeration field is empty")
    private Boolean requestModeration;
    @NotEmpty(message = "The title field is empty")
    private String title;
}
