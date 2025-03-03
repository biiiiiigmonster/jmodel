CREATE TABLE t_user
(
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(50) NOT NULL,
    email VARCHAR(100)
);

CREATE TABLE t_phone
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    number  VARCHAR(20) NOT NULL,
    user_id BIGINT
);

CREATE TABLE t_history
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    summary  VARCHAR(20) NOT NULL,
    phone_id BIGINT
);