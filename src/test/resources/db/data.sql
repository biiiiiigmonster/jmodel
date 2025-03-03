INSERT INTO user (id, name, email)
VALUES (1, 'John Doe', 'john@example.com'),
       (2, 'Jane Smith', 'jane@example.com');

INSERT INTO phone (id, number, user_id)
VALUES (1, '1234567890', 1),
       (2, '5555555555', 2);

INSERT INTO history (id, summary, phone_id)
VALUES (1, 'call Jane 1', 1),
       (2, 'call Jane 2', 1),
       (3, 'callback John', 2);