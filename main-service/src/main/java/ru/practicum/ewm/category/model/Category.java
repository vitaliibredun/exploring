package ru.practicum.ewm.category.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "categories", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", unique = true)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        return id != null && id.equals(((Category) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
