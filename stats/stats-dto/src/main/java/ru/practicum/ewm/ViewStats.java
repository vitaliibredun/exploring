package ru.practicum.ewm;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}
