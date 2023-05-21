-- Basic data for the comments feature test

INSERT INTO users (id, name, email)
VALUES (100, 'Lex Luthor', 'lex.ne@mail.com');

INSERT INTO users (id, name, email)
VALUES (101, 'Clark Kent', 'superklarkn@mail.com');

INSERT INTO users (id, name, email)
VALUES (102, 'Diana Prince', 'dianawonder@mail.com');

INSERT INTO users (id, name, email)
VALUES (103, 'Bruce Wayne', 'iambatman@mail.com');

INSERT INTO categories (id, name)
VALUES (100, 'Kill superman');



INSERT INTO locations (id, lat, lon)
VALUES (100, 55.754167, 37.62);



INSERT INTO events (id, annotation, created_on, category, description, event_date, initiator, location, paid,
                    participant_limit, published_on, request_moderation, title, state)
VALUES (100, 'Сап прогулки по рекам и каналам – это возможность увидеть Практикбург с другого ракурса', '2019-11-01 15:00:00',
        100, 'Гавайская разновидность сёрфинга, в котором серфер, стоя на доске, катается на волнах',
        '2020-01-01 15:00:00', 100, 100, true, 0, '2019-12-01 15:00:00', false, 'Сап прогулки по рекам и каналам', 'PUBLISHED');

INSERT INTO events (id, annotation, created_on, category, description, event_date, initiator, location, paid,
                    participant_limit, published_on, request_moderation, title, state)
VALUES (101, 'Сап прогулки по рекам и каналам – это возможность увидеть Практикбург с другого ракурса', '2019-11-01 15:00:00',
        100, 'Гавайская разновидность сёрфинга, в котором серфер, стоя на доске, катается на волнах',
        '2222-01-01 15:00:00', 100, 100, true, 0, '2019-12-01 15:00:00', false, 'Сап прогулки по рекам и каналам', 'PUBLISHED');



INSERT INTO requests (id, created, event, requester, status)
VALUES (100, '2019-11-10 15:00:00', 100, 101, 'CONFIRMED');

INSERT INTO requests (id, created, event, requester, status)
VALUES (101, '2019-11-15 15:00:00', 100, 102, 'CONFIRMED');

INSERT INTO requests (id, created, event, requester, status)
VALUES (102, '2021-11-10 15:00:00', 101, 101, 'CONFIRMED');