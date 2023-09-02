package ru.practicum.ewm.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @NotEmpty(message = "The field of name  is empty")
    private String name;
    @NotEmpty(message = "The field of email  is empty")
    @Email(message = "The incorrect type of email")
    private String email;
}
