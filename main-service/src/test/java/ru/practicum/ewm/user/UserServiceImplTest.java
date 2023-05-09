package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.UserService;

import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
public class UserServiceImplTest {
    private final UserService service;
    private final UserRepository repository;
    private final EntityManager entityManager;
    private UserDto userDto1;
    private UserDto userDto2;
    private UserDto userDto3;
    private UserDto userDto4;
    private UserDto userDto5;

    @BeforeEach
    void setUp() {
        userDto1 = makeUserDto("John", "mail@my.com");
        userDto2 = makeUserDto("Smith", "email@ne.com");
        userDto3 = makeUserDto("Klark", "supermail@my.com");
        userDto4 = makeUserDto("Kent", "for@me.com");
        userDto5 = makeUserDto("Timmy", "timmy@timmy.com");
        resetIdColumns();
    }

    @Test
    void addUserTest() {
        assertThat(repository.findAll(), empty());

        UserDto userFromRepository = service.addUser(userDto1);

        assertThat(userFromRepository.getId(), notNullValue());
        assertThat(userDto1.getName(), is(userFromRepository.getName()));
        assertThat(userDto1.getEmail(), is(userFromRepository.getEmail()));
    }

    @Test
    void getUsersWithParametersTest() {
        assertThat(repository.findAll(), empty());

        Integer expectedSize = 1;
        List<Long> userIds = List.of(1L, 2L, 3L, 4L, 5L);
        PageRequest pageRequest = PageRequest.of(2, 2);
        service.addUser(userDto1);
        service.addUser(userDto2);
        service.addUser(userDto3);
        service.addUser(userDto4);
        service.addUser(userDto5);

        List<UserDto> users = service.getUsers(userIds, pageRequest);
        UserDto userFromRepository = users.get(0);

        assertThat(users.size(), is(expectedSize));
        assertThat(userFromRepository.getId(), notNullValue());
        assertThat(userDto5.getName(), is(userFromRepository.getName()));
        assertThat(userDto5.getEmail(), is(userFromRepository.getEmail()));
    }

    @Test
    void getUsersWithoutParametersTest() {
        assertThat(repository.findAll(), empty());

        Integer expectedSize = 5;
        List<Long> userIds = null;
        PageRequest pageRequest = PageRequest.of(0, 10);
        service.addUser(userDto1);
        service.addUser(userDto2);
        service.addUser(userDto3);
        service.addUser(userDto4);
        service.addUser(userDto5);

        List<UserDto> users = service.getUsers(userIds, pageRequest);
        UserDto userFromRepository1 = users.get(0);
        UserDto userFromRepository2 = users.get(1);
        UserDto userFromRepository3 = users.get(2);
        UserDto userFromRepository4 = users.get(3);
        UserDto userFromRepository5 = users.get(4);

        assertThat(users.size(), is(expectedSize));
        assertThat(userDto1.getName(), is(userFromRepository1.getName()));
        assertThat(userDto1.getEmail(), is(userFromRepository1.getEmail()));
        assertThat(userDto2.getName(), is(userFromRepository2.getName()));
        assertThat(userDto2.getEmail(), is(userFromRepository2.getEmail()));
        assertThat(userDto3.getName(), is(userFromRepository3.getName()));
        assertThat(userDto3.getEmail(), is(userFromRepository3.getEmail()));
        assertThat(userDto4.getName(), is(userFromRepository4.getName()));
        assertThat(userDto4.getEmail(), is(userFromRepository4.getEmail()));
        assertThat(userDto5.getName(), is(userFromRepository5.getName()));
        assertThat(userDto5.getEmail(), is(userFromRepository5.getEmail()));
    }

    @Test
    void deleteUserTest() {
        UserDto userFromRepository = service.addUser(userDto1);

        assertThat(repository.findAll(), notNullValue());

        service.deleteUser(userFromRepository.getId());

        assertThat(repository.findAll(), empty());
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto.UserDtoBuilder builder = UserDto.builder();

        builder.name(name);
        builder.email(email);

        return builder.build();
    }

    private void resetIdColumns() {
        entityManager
                .createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}