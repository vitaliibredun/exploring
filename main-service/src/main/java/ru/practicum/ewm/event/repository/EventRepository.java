package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.event.constants.EventState;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select e " +
            "from Event e " +
            "where e.initiator.id = ?1")
    List<Event> findAllByUserId(Long userId, Pageable pageable);

    @Query("select e " +
            "from Event e " +
            "where (e.initiator.id in ?1 or ?1 is null) " +
            "and (e.state in ?2 or ?2 is null) " +
            "and (e.category.id in ?3 or ?3 is null) " +
            "and (e.eventDate > ?4 or ?4 is null) " +
            "and (e.eventDate < ?5 or ?5 is null)")
    List<Event> searchEventsByAdmin(List<Long> users,
                                    List<EventState> states,
                                    List<Long> categories,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    Pageable pageable);

    @Query("select e " +
            "from Event e " +
            "where e.state = ru.practicum.ewm.event.constants.EventState.PUBLISHED " +
            "and (e.category.id in ?2 or ?2 is null) " +
            "and (e.paid = ?3 or ?3 is null) " +
            "and (e.eventDate > ?4 or e.eventDate > current_timestamp) " +
            "and (e.eventDate < ?5 or e.eventDate > current_timestamp) " +
            "and (upper(e.annotation) like upper(concat('%', ?1, '%')) or ?1 is null) " +
            "or (upper(e.annotation) like upper(concat('%', ?1)) or ?1 is null) " +
            "or (upper(e.annotation) like upper(concat(?1, '%')) or ?1 is null) " +
            "or (upper(e.description) like upper(concat('%', ?1, '%')) or ?1 is null) " +
            "or (upper(e.description) like upper(concat('%', ?1)) or ?1 is null) " +
            "or (upper(e.description) like upper(concat(?1, '%')) or ?1 is null )")
    List<Event> searchEventsByUser(String text,
                                   List<Long> categories,
                                   Boolean paid,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Pageable pageable);

    @Query("select e " +
            "from Event e " +
            "where e.id = ?1 " +
            "and e.state = ru.practicum.ewm.event.constants.EventState.PUBLISHED")
    Event findEvent(Long id);
}
