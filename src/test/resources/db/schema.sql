CREATE TABLE t_user
(
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(50) NOT NULL,
    email VARCHAR(100)
);

CREATE TABLE t_profile
(
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    description  VARCHAR(50) NOT NULL,
    user_id BIGINT
);