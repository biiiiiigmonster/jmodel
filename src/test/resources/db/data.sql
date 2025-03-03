INSERT INTO t_user (name, email)
VALUES ('John Doe', 'john@example.com'),
       ('Jane Smith', 'jane@example.com');

INSERT INTO t_phone (number, user_id)
VALUES ('1234567890', 1),
       ('5555555555', 2);

INSERT INTO t_history (summary, phone_id)
VALUES ('call Jane 1', 1),
       ('call Jane 2', 1),
       ('callback John', 2);