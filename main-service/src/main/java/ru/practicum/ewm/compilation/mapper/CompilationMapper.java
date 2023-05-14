package ru.practicum.ewm.compilation.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

@Mapper
public interface CompilationMapper {
    default Compilation toModel(NewCompilationDto compilationDto, List<Event> events) {
        if (compilationDto == null) {
            return null;
        }

        Compilation.CompilationBuilder builder = Compilation.builder();

        builder.events(events);
        if (compilationDto.getPinned() == null) {
            builder.pinned(false);
        }
        builder.pinned(compilationDto.getPinned());
        builder.title(compilationDto.getTitle());

        return builder.build();
    }

    default CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {

        if (compilation == null) {
            return null;
        }

        CompilationDto.CompilationDtoBuilder builder = CompilationDto.builder();

        builder.id(compilation.getId());
        builder.pinned(compilation.getPinned());
        builder.title(compilation.getTitle());
        builder.events(events);


        return builder.build();
    }
}
