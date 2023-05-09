package ru.practicum.ewm.category.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {
    @NotEmpty(message = "The name field is empty")
    private String name;
}
