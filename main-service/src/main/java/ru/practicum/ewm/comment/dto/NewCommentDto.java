package ru.practicum.ewm.comment.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    @NotEmpty(message = "The text field is empty")
    private String text;
}
