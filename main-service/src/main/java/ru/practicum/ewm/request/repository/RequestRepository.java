package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("select r " +
            "from Request r " +
            "where r.event.initiator.id = ?1 " +
            "and r.event.id = ?2")
    List<Request> findAllBy(Long userId, Long eventId);

    @Query("select r " +
            "from Request r " +
            "where r.requester.id = ?1 " +
            "and r.event.id = ?2")
    Optional<Request> findByUserAndEvent(Long userId, Long eventId);

    @Query("select count (r.id) " +
            "from Request r " +
            "where r.event.id = ?1 " +
            "and r.event.participantLimit != 0 " +
            "and r.status = ru.practicum.ewm.request.constants.RequestState.CONFIRMED")
    Long findQuantityAllConfirmed(Long eventId);

    @Query("select r " +
            "from Request r " +
            "where r.requester.id = ?1")
    List<Request> findAllRequestsByUser(Long userId);
}
