package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stat, Integer> {

    @Query("select new ru.practicum.ewm.ViewStats" +
            "(s.app, s.uri, count (s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count (s.ip) desc")
    List<ViewStats> findALLStats(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewm.ViewStats" +
            "(s.app, s.uri,  (count (s.ip)) / (count (s.ip)))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri, s.ip " +
            "having count (s.ip) > 1 " +
            "order by count (s.ip) desc")
    List<ViewStats> findALLStatsWithUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewm.ViewStats" +
            "(s.app, s.uri, (count (s.ip)) / (count (s.ip)))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri, s.ip " +
            "having count (s.ip) > 1 " +
            "order by count (s.ip) desc")
    List<ViewStats> findStatsWithUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ewm.ViewStats" +
            "(s.app, s.uri, count (s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri " +
            "order by count (s.ip) desc")
    List<ViewStats> findStatsByUriList(LocalDateTime start, LocalDateTime end, List<String> uris);
}