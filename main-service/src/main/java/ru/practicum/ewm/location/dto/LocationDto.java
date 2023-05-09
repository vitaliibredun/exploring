package ru.practicum.ewm.location.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @NotNull(message = "The latitude field is empty")
    private Float latitude;
    @NotNull(message = "The longitude field is empty")
    private Float longitude;
}
