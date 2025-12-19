-- Users
INSERT INTO t_user (id, name, email)
VALUES (1, 'John Doe', 'john.doe@example.com'),
       (2, 'Jane Smith', 'jane.smith@example.com'),
       (3, 'Michael Johnson', 'michael.j@example.com');

-- Posts
INSERT INTO t_post (id, user_id, title)
VALUES (1, 1, 'Getting Started with Spring Boot'),
       (2, 1, 'Mastering JPA Relationships'),
       (3, 2, 'Introduction to React Hooks'),
       (4, 2, 'Advanced TypeScript Patterns'),
       (5, 3, 'Docker Best Practices');
