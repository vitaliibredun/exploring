package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.NotExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository repository;
    private final CompilationMapper compilationMapper;
    private final EventService eventService;
    private final EventMapper eventMapper;

    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        checkCompilationOnNull(compilationDto);
        Set<Long> eventsIds = compilationDto.getEvents();
        List<Event> events = checkListEvents(eventsIds);
        Compilation compilation = compilationMapper.toModel(compilationDto, events);
        Compilation compilationFromRepository = repository.save(compilation);
        List<EventShortDto> shortEvents = events.stream().map(eventMapper::toShortDto).collect(Collectors.toList());
        return compilationMapper.toDto(compilationFromRepository, shortEvents);
    }

    @Override
    public void deleteCompilation(Long compId) {
        checkIfCompilationExist(compId);
        repository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = checkIfCompilationExist(compId);
        Compilation updatedCompilation = doUpdateCompilation(compilation, updateCompilationDto);
        Compilation compilationFromRepository = repository.save(updatedCompilation);
        List<EventShortDto> shortEvents = updatedCompilation.getEvents()
                .stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
        return compilationMapper.toDto(compilationFromRepository, shortEvents);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        List<CompilationDto> compilations = new ArrayList<>();
        List<Compilation> compilationsFromRepository = repository.findAllByParameters(pinned, pageable);
        for (Compilation compilation : compilationsFromRepository) {
            List<Event> events = compilation.getEvents();
            List<EventShortDto> shortEvents = events.stream().map(eventMapper::toShortDto).collect(Collectors.toList());
            CompilationDto compilationDto = compilationMapper.toDto(compilation, shortEvents);
            compilations.add(compilationDto);
        }
        return compilations;
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = checkIfCompilationExist(compId);
        List<Event> events = compilation.getEvents();
        List<EventShortDto> shortEvents = events.stream().map(eventMapper::toShortDto).collect(Collectors.toList());
        return compilationMapper.toDto(compilation, shortEvents);
    }

    private Compilation doUpdateCompilation(Compilation compilation, UpdateCompilationDto updateCompilationDto) {
        boolean newEventsIds = updateCompilationDto.getEvents() != null;
        boolean newTitle = updateCompilationDto.getTitle() != null;
        boolean newPinnedStatus = updateCompilationDto.getPinned() != null;

        if (newEventsIds) {
            Set<Long> eventsIds = updateCompilationDto.getEvents();
            List<Event> events = checkListEvents(eventsIds);
            compilation.setEvents(events);
        }
        if (newPinnedStatus) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (newTitle) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        return compilation;
    }

    private Compilation checkIfCompilationExist(Long compId) {
        Optional<Compilation> compilation = repository.findById(compId);
        if (compilation.isEmpty()) {
            log.error("Error: the required compilation with id = {} was not found", compId);
            throw new NotExistsException("Compilation was not found");
        }
        return compilation.get();
    }

    private List<Event> checkListEvents(Set<Long> eventsIds) {
        boolean noIds = (eventsIds == null) || (eventsIds.isEmpty());
        if (noIds) {
            return new ArrayList<>();
        }
        return eventService.getAllEventsByIds(eventsIds);
    }

    private void checkCompilationOnNull(NewCompilationDto compilationDto) {
        boolean eventsIsNull = compilationDto.getEvents() == null;
        boolean pinnedIsNull = compilationDto.getPinned() == null;
        boolean titleIsNull = compilationDto.getTitle() == null;
        if (eventsIsNull || pinnedIsNull || titleIsNull) {
            log.error("Error: the fields of compilation must not be null");
            throw new BadRequestException("The fields of compilation must not be null");
        }
    }
}
