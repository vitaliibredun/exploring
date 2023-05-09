package ru.practicum.ewm.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationDto {
    private Set<Long> events;
    private Boolean pinned;
    @NotEmpty(message = "The title field is empty")
    private String title;
}
