package ru.practicum.ewm.category.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @NotEmpty(message = "The field of name is empty")
    private String name;
}