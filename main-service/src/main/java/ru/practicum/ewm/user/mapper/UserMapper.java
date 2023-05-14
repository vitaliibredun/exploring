package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

@Mapper
public interface UserMapper {
    User toModel(UserDto userDto);

    UserDto toDto(User user);
}
