package ru.practicum.ewm.compilation;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
public class CompilationServiceImplTest {
    private final CompilationService compilationService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final EventService eventService;
    private final CompilationRepository repository;
    private final EntityManager entityManager;
    private NewCompilationDto newCompilationDto1;
    private NewCompilationDto newCompilationDto2;
    private NewCompilationDto newCompilationDto3;
    private UpdateCompilationDto updateCompilationDto;

    @BeforeEach
    void setUp() {
        NewCategoryDto newCategoryDto1 = Instancio.create(NewCategoryDto.class);
        NewCategoryDto newCategoryDto2 = Instancio.create(NewCategoryDto.class);
        NewCategoryDto newCategoryDto3 = Instancio.create(NewCategoryDto.class);

        categoryService.addCategory(newCategoryDto1);
        categoryService.addCategory(newCategoryDto2);
        categoryService.addCategory(newCategoryDto3);

        UserDto userDto = Instancio.of(UserDto.class).ignore(field(UserDto::getId)).create();
        userService.addUser(userDto);

        NewEventDto newEventDto1 = makeNewEventDto(1L);
        NewEventDto newEventDto2 = makeNewEventDto(1L);
        NewEventDto newEventDto3 = makeNewEventDto(2L);
        NewEventDto newEventDto4 = makeNewEventDto(2L);
        NewEventDto newEventDto5 = makeNewEventDto(3L);
        eventService.addEvent(1L, newEventDto1);
        eventService.addEvent(1L, newEventDto2);
        eventService.addEvent(1L, newEventDto3);
        eventService.addEvent(1L, newEventDto4);
        eventService.addEvent(1L, newEventDto5);

        newCompilationDto1 = makeNewCompilationDto();
        newCompilationDto2 = makeNewCompilationDto();
        newCompilationDto3 = makeNewCompilationDto();
        updateCompilationDto = makeUpdateCompilationDto();

        resetIdColumns();
    }

    @Test
    void addCompilationTest() {
        assertThat(repository.findAll(), empty());

        List<Long> list = new ArrayList<>(newCompilationDto1.getEvents());
        List<Long> eventsIdS = list.stream().sorted().collect(Collectors.toList());
        Long firstEventId = eventsIdS.get(0);
        CompilationDto compilationFromRepository = compilationService.addCompilation(newCompilationDto1);
        List<Long> events = compilationFromRepository.getEvents()
                .stream()
                .map(EventShortDto::getId)
                .sorted()
                .collect(Collectors.toList());
        Long firstEventIdFromRepository = events.get(0);

        assertThat(compilationFromRepository.getId(), notNullValue());
        assertThat(newCompilationDto1.getPinned(), is(compilationFromRepository.getPinned()));
        assertThat(newCompilationDto1.getTitle(), is(compilationFromRepository.getTitle()));
        assertThat(newCompilationDto1.getEvents().size(), is(compilationFromRepository.getEvents().size()));
        assertThat(firstEventId, is(firstEventIdFromRepository));
    }

    @Test
    void addCompilationWithoutEventsTest() {
        assertThat(repository.findAll(), empty());

        newCompilationDto1.setEvents(new HashSet<>());
        CompilationDto compilationFromRepository = compilationService.addCompilation(newCompilationDto1);

        assertThat(compilationFromRepository.getId(), notNullValue());
        assertThat(newCompilationDto1.getPinned(), is(compilationFromRepository.getPinned()));
        assertThat(newCompilationDto1.getTitle(), is(compilationFromRepository.getTitle()));
        assertThat(compilationFromRepository.getEvents(), empty());
    }

    @Test
    void deleteCompilationTest() {
        Long comId = 1L;
        compilationService.addCompilation(newCompilationDto1);

        assertThat(repository.findAll().size(), notNullValue());

        compilationService.deleteCompilation(comId);

        assertThat(repository.findAll(), empty());
    }

    @Test
    void verifyDeleteCompilationExistException() {
        Long comId = 1L;

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> compilationService.deleteCompilation(comId));

        assertThat("Compilation was not found", is(exception.getMessage()));
    }

    @Test
    void updateCompilationTest() {
        Long comId = 1L;
        CompilationDto firstCompilation = compilationService.addCompilation(newCompilationDto1);

        CompilationDto updatedCompilation = compilationService.updateCompilation(comId, updateCompilationDto);

        assertThat(firstCompilation.getId(), is(updatedCompilation.getId()));
        assertThat(firstCompilation.getEvents(), not(updatedCompilation.getEvents()));
        assertThat(firstCompilation.getEvents().size(), not(updatedCompilation.getEvents().size()));
        assertThat(firstCompilation.getTitle(), not(updatedCompilation.getTitle()));
        assertThat(firstCompilation.getPinned(), not(updatedCompilation.getPinned()));
    }

    @Test
    void verifyUpdateCompilationExistException() {
        Long wrongComId = 100L;
        compilationService.addCompilation(newCompilationDto1);

        final NotExistsException exception = assertThrows(
                NotExistsException.class,
                () -> compilationService.updateCompilation(wrongComId, updateCompilationDto));

        assertThat("Compilation was not found", is(exception.getMessage()));
    }

    @Test
    void getCompilationsTest() {
        Integer expectedSize = 3;
        Boolean pinned = true;
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Long> list1 = new ArrayList<>(newCompilationDto1.getEvents());
        List<Long> eventsCompilation1 = list1.stream().sorted().collect(Collectors.toList());
        Long eventId1 = eventsCompilation1.get(0);
        List<Long> list2 = new ArrayList<>(newCompilationDto3.getEvents());
        List<Long> eventsCompilation3 = list2.stream().sorted().collect(Collectors.toList());
        Long eventId5 = eventsCompilation3.get(4);
        newCompilationDto1.setEvents(Set.of(1L, 2L));
        compilationService.addCompilation(newCompilationDto1);
        newCompilationDto2.setEvents(Set.of(3L, 4L));
        compilationService.addCompilation(newCompilationDto2);
        newCompilationDto3.setEvents(Set.of(5L));
        compilationService.addCompilation(newCompilationDto3);

        List<CompilationDto> compilations = compilationService.getCompilations(pinned, pageRequest);
        CompilationDto compilationFromRepository1 = compilations.get(0);
        CompilationDto compilationFromRepository2 = compilations.get(2);
        List<Long> eventsByCompilation1 = compilationFromRepository1.getEvents()
                .stream()
                .map(EventShortDto::getId)
                .sorted()
                .collect(Collectors.toList());
        Long eventIdFromRepository1 = eventsByCompilation1.get(0);
        List<Long> eventsByCompilation2 = compilationFromRepository2.getEvents()
                .stream()
                .map(EventShortDto::getId)
                .sorted()
                .collect(Collectors.toList());
        Long eventIdFromRepository5 = eventsByCompilation2.get(0);

        assertThat(compilations.size(), is(expectedSize));
        assertThat(newCompilationDto1.getTitle(), is(compilationFromRepository1.getTitle()));
        assertThat(newCompilationDto1.getPinned(), is(compilationFromRepository1.getPinned()));
        assertThat(newCompilationDto3.getTitle(), is(compilationFromRepository2.getTitle()));
        assertThat(newCompilationDto3.getPinned(), is(compilationFromRepository2.getPinned()));
        assertThat(eventId1, is(eventIdFromRepository1));
        assertThat(eventId5, is(eventIdFromRepository5));
    }

    @Test
    void getCompilationTest() {
        Long compId = 1L;
        List<Long> listIds = new ArrayList<>(newCompilationDto1.getEvents());
        List<Long> eventsIdS = listIds.stream().sorted().collect(Collectors.toList());
        Long firstEventId = eventsIdS.get(0);
        Long secondEventId = eventsIdS.get(1);
        compilationService.addCompilation(newCompilationDto1);

        CompilationDto compilationFromRepository = compilationService.getCompilation(compId);
        List<Long> events = compilationFromRepository.getEvents()
                .stream()
                .map(EventShortDto::getId)
                .sorted()
                .collect(Collectors.toList());
        Long firstEventIdFromRepository = events.get(0);
        Long secondEventIdFromRepository = events.get(1);

        assertThat(newCompilationDto1.getPinned(), is(compilationFromRepository.getPinned()));
        assertThat(newCompilationDto1.getTitle(), is(compilationFromRepository.getTitle()));
        assertThat(newCompilationDto1.getEvents().size(), is(compilationFromRepository.getEvents().size()));
        assertThat(firstEventId, is(firstEventIdFromRepository));
        assertThat(secondEventId, is(secondEventIdFromRepository));
    }

    private NewEventDto makeNewEventDto(Long category) {

        return Instancio.of(NewEventDto.class)
                .set(field(NewEventDto::getCategory), category)
                .set(field(NewEventDto::getEventDate),LocalDateTime.now().plusDays(1))
                .create();
    }

    private UpdateCompilationDto makeUpdateCompilationDto() {

        return Instancio.of(UpdateCompilationDto.class)
                .set(field(UpdateCompilationDto::getEvents), Set.of(3L, 4L, 5L))
                .set(field(UpdateCompilationDto::getPinned), false)
                .create();
    }

    private NewCompilationDto makeNewCompilationDto() {
        return Instancio.of(NewCompilationDto.class)
                .set(field(NewCompilationDto::getEvents), Set.of(1L, 2L, 3L, 4L, 5L))
                .set(field(NewCompilationDto::getPinned),true)
                .create();
    }

    private void resetIdColumns() {
        entityManager
                .createNativeQuery("ALTER TABLE compilations ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE events ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
