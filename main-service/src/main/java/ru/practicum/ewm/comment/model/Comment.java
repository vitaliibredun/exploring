package ru.practicum.ewm.comment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.comment.constants.CommentStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "comments", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text")
    private String text;
    @ManyToOne
    @JoinColumn(name = "event", referencedColumnName = "id")
    private Event event;
    @ManyToOne
    @JoinColumn(name = "author", referencedColumnName = "id")
    private User author;
    @Column(name = "created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        return id != null && id.equals(((Comment) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
