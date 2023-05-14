package ru.practicum.ewm.compilation.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationDto {
    private Set<Long> events;
    private Boolean pinned;
    private String title;
}
