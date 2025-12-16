DROP TABLE IF EXISTS t_taggable;
DROP TABLE IF EXISTS t_user_role;
DROP TABLE IF EXISTS t_likes;
DROP TABLE IF EXISTS t_comment;
DROP TABLE IF EXISTS t_image;
DROP TABLE IF EXISTS t_post;
DROP TABLE IF EXISTS t_address;
DROP TABLE IF EXISTS t_profile;
DROP TABLE IF EXISTS t_tag;
DROP TABLE IF EXISTS t_role;
DROP TABLE IF EXISTS t_video;
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user
(
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(50) NOT NULL,
    email VARCHAR(100)
);

CREATE TABLE t_profile
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    description VARCHAR(50) NOT NULL,
    user_id     BIGINT
);

CREATE TABLE t_address
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    profile_id BIGINT,
    location   VARCHAR(255)
);

CREATE TABLE t_comment
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    content          VARCHAR(255),
    commentable_id   BIGINT,
    commentable_type VARCHAR(100)
);

CREATE TABLE t_image
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    url            VARCHAR(255),
    imageable_id   BIGINT,
    imageable_type VARCHAR(100)
);

CREATE TABLE t_post
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    title   VARCHAR(255)
);

CREATE TABLE t_role
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);

CREATE TABLE t_tag
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(50),
    parent_id BIGINT
);

CREATE TABLE t_taggable
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_id        BIGINT,
    taggable_id   BIGINT,
    taggable_type VARCHAR(100)
);

CREATE TABLE t_user_role
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    role_id BIGINT
);

CREATE TABLE t_video
(
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),
    url   VARCHAR(255)
);

CREATE TABLE t_likes
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT,
    praise  VARCHAR(255)
);