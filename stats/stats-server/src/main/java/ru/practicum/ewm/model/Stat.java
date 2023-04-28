package ru.practicum.ewm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(name = "statistics", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "app")
    private String app;
    @Column(name = "uri")
    private String uri;
    @Column(name = "ip")
    private String ip;
    @Column(name = "time_stamp")
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stat)) return false;
        return id != null && id.equals(((Stat) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
