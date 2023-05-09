package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        try {
            checkUserOnNull(userDto);
            User user = mapper.toModel(userDto);
            User userFromRepository = repository.save(user);
            return mapper.toDto(userFromRepository);
        } catch (DataIntegrityViolationException exception) {
            log.error("Field: name. Error: the same email");
            throw new ConflictException("The field of email must be unique");
        }
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        return repository.findAllByIds(ids, pageable)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        repository.deleteById(userId);
    }

    @Override
    public User getUser(Long userId) {
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            log.error("User with id = {} was not found", userId);
            throw new NotExistsException("User with id was not found");
        }
        return user.get();
    }

    private void checkUserOnNull(UserDto userDto) {
        boolean noName = userDto.getName() == null;
        boolean noEmail = userDto.getEmail() == null;
        if (noName || noEmail) {
            log.error("Error: the fields of user must not be empty");
            throw new BadRequestException("The fields of user must not be empty");
        }
    }
}
