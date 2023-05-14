package ru.practicum.ewm.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    @NotNull
    private Set<Long> events;
    @NotNull
    private Boolean pinned;
    @NotEmpty(message = "The field of title is empty")
    private String title;
}
