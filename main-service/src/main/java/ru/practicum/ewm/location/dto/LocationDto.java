package ru.practicum.ewm.location.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @NotNull(message = "The field of latitude  is empty")
    private Float latitude;
    @NotNull(message = "The field of longitude  is empty")
    private Float longitude;
}
