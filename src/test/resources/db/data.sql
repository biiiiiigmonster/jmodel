-- Users (基础表 10条)
INSERT INTO t_user (id, name, email)
VALUES (1, 'John Doe', 'john.doe@example.com'),
       (2, 'Jane Smith', 'jane.smith@example.com'),
       (3, 'Michael Johnson', 'michael.j@example.com'),
       (4, 'Emily Brown', 'emily.b@example.com'),
       (5, 'David Wilson', 'david.w@example.com'),
       (6, 'Sarah Davis', 'sarah.d@example.com'),
       (7, 'James Miller', 'james.m@example.com'),
       (8, 'Lisa Anderson', 'lisa.a@example.com'),
       (9, 'Robert Taylor', 'robert.t@example.com'),
       (10, 'Jennifer White', 'jennifer.w@example.com'),
       (11, 'Michael Jordan', 'michael.j@example.com');

-- Profiles (基础表 10条，与User一对一关联)
INSERT INTO t_profile (id, description, user_id)
VALUES (1, 'Software Engineer at Tech Corp', 1),
       (2, 'Digital Marketing Specialist', 2),
       (3, 'Product Manager', 3),
       (4, 'UX Designer', 4),
       (5, 'Data Scientist', 5),
       (6, 'Content Creator', 6),
       (7, 'Full Stack Developer', 7),
       (8, 'Business Analyst', 8),
       (9, 'Project Manager', 9),
       (10, 'UI Designer', 10);

-- Addresses (基础表 10条，与Profile一对一关联)
INSERT INTO t_address (id, profile_id, location)
VALUES (1, 1, 'New York, NY'),
       (2, 2, 'San Francisco, CA'),
       (3, 3, 'Chicago, IL'),
       (4, 4, 'Los Angeles, CA'),
       (5, 5, 'Boston, MA'),
       (6, 6, 'Seattle, WA'),
       (7, 7, 'Austin, TX'),
       (8, 8, 'Portland, OR'),
       (9, 9, 'Miami, FL'),
       (10, 10, 'Denver, CO');

-- Posts (基础表 10条，与User多对一关联)
INSERT INTO t_post (id, user_id, title)
VALUES (1, 1, 'Getting Started with Spring Boot'),
       (2, 1, 'Mastering JPA Relationships'),
       (3, 2, 'Introduction to React Hooks'),
       (4, 2, 'Advanced TypeScript Patterns'),
       (5, 3, 'Docker Best Practices'),
       (6, 4, 'Microservices Architecture'),
       (7, 5, 'Cloud Native Applications'),
       (8, 6, 'API Design Principles'),
       (9, 7, 'Testing Strategies'),
       (10, 8, 'DevOps Pipeline Setup');

-- Videos (基础表 10条)
INSERT INTO t_video (id, title, url)
VALUES (1, 'Spring Boot Tutorial', 'https://example.com/videos/spring-boot'),
       (2, 'React Fundamentals', 'https://example.com/videos/react'),
       (3, 'Docker Deep Dive', 'https://example.com/videos/docker'),
       (4, 'Kubernetes Basics', 'https://example.com/videos/kubernetes'),
       (5, 'AWS Services Overview', 'https://example.com/videos/aws'),
       (6, 'MongoDB Tutorial', 'https://example.com/videos/mongodb'),
       (7, 'GraphQL Introduction', 'https://example.com/videos/graphql'),
       (8, 'Vue.js Essentials', 'https://example.com/videos/vuejs'),
       (9, 'Python for Beginners', 'https://example.com/videos/python'),
       (10, 'JavaScript ES6+', 'https://example.com/videos/javascript');

-- Roles (基础表 10条)
INSERT INTO t_role (id, name)
VALUES (1, 'Administrator'),
       (2, 'Moderator'),
       (3, 'Editor'),
       (4, 'Author'),
       (5, 'Contributor'),
       (6, 'Reviewer'),
       (7, 'Developer'),
       (8, 'Designer'),
       (9, 'Tester'),
       (10, 'Guest');

-- Tags (基础表 10条)
INSERT INTO t_tag (id, name, parent_id)
VALUES (1, 'Java', 0),
       (2, 'Spring', 1),
       (3, 'JavaScript', 0),
       (4, 'React', 3),
       (5, 'Docker', 0),
       (6, 'Cloud', 5),
       (7, 'Database', 1),
       (8, 'Security', 2),
       (9, 'Testing', 0),
       (10, 'DevOps', 6);

-- Comments (基础表 10条，多态关联到Post或Video)
INSERT INTO t_comment (id, content, commentable_id, commentable_type)
VALUES (1, 'Great tutorial!', 1, 'Post'),
       (2, 'Very helpful content', 1, 'Post'),
       (3, 'Nice explanation', 2, 'Post'),
       (4, 'Looking forward to more', 3, 'Post'),
       (5, 'Excellent video!', 1, 'Video'),
       (6, 'Well explained', 2, 'Video'),
       (7, 'This helped me a lot', 4, 'Post'),
       (8, 'Amazing content', 3, 'Video'),
       (9, 'Keep it up!', 5, 'Post'),
       (10, 'Very informative', 4, 'Video');

-- Images (基础表 10条，多态关联到User或Post)
INSERT INTO t_image (id, url, imageable_id, imageable_type)
VALUES (1, 'https://example.com/images/user1.jpg', 1, 'User'),
       (2, 'https://example.com/images/post1.jpg', 1, 'Post'),
       (3, 'https://example.com/images/user2.jpg', 2, 'User'),
       (4, 'https://example.com/images/post2.jpg', 2, 'Post'),
       (5, 'https://example.com/images/user3.jpg', 3, 'User'),
       (6, 'https://example.com/images/post3.jpg', 3, 'Post'),
       (7, 'https://example.com/images/user4.jpg', 4, 'User'),
       (8, 'https://example.com/images/post4.jpg', 4, 'Post'),
       (9, 'https://example.com/images/user5.jpg', 5, 'User'),
       (10, 'https://example.com/images/post5.jpg', 5, 'Post');

-- UserRole (Pivot中间表 20条，用户-角色多对多关联)
INSERT INTO t_user_role (id, user_id, role_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 1, 3),
       (4, 2, 2),
       (5, 2, 3),
       (6, 3, 3),
       (7, 3, 4),
       (8, 4, 4),
       (9, 4, 5),
       (10, 5, 1),
       (11, 5, 3),
       (12, 6, 2),
       (13, 6, 4),
       (14, 7, 3),
       (15, 7, 5),
       (16, 8, 4),
       (17, 8, 5),
       (18, 9, 1),
       (19, 9, 2),
       (20, 10, 2);

-- Taggable (MorphPivot多态中间表 20条，标签多态关联到Post或Video)
INSERT INTO t_taggable (id, tag_id, taggable_id, taggable_type)
VALUES (1, 1, 1, 'Post'),
       (2, 2, 1, 'Post'),
       (3, 3, 2, 'Post'),
       (4, 4, 2, 'Post'),
       (5, 5, 3, 'Post'),
       (6, 6, 3, 'Post'),
       (7, 7, 4, 'Post'),
       (8, 8, 4, 'Post'),
       (9, 9, 5, 'Post'),
       (10, 10, 5, 'Post'),
       (11, 1, 1, 'Video'),
       (12, 2, 1, 'Video'),
       (13, 3, 2, 'Video'),
       (14, 4, 2, 'Video'),
       (15, 5, 3, 'Video'),
       (16, 6, 3, 'Video'),
       (17, 7, 4, 'Video'),
       (18, 8, 4, 'Video'),
       (19, 9, 5, 'Video'),
       (20, 10, 5, 'Video'),
       (21, 1, 6, 'Post'),
       (22, 2, 6, 'Post'),
       (23, 3, 7, 'Post'),
       (24, 4, 7, 'Post'),
       (25, 5, 8, 'Post'),
       (26, 6, 8, 'Post'),
       (27, 7, 9, 'Post'),
       (28, 8, 9, 'Post'),
       (29, 9, 10, 'Post'),
       (30, 10, 10, 'Post'),
       (31, 1, 6, 'Video'),
       (32, 2, 6, 'Video'),
       (33, 3, 7, 'Video'),
       (34, 4, 7, 'Video'),
       (35, 5, 8, 'Video'),
       (36, 6, 8, 'Video'),
       (37, 7, 9, 'Video'),
       (38, 8, 9, 'Video'),
       (39, 9, 10, 'Video'),
       (40, 10, 10, 'Video'),
       (41, 1, 1, 'Phone'),
       (42, 2, 1, 'Phone'),
       (43, 3, 2, 'Phone'),
       (44, 4, 2, 'Phone'),
       (45, 5, 3, 'Phone');

-- Likes (基础表 20条，与Post多对一关联)
INSERT INTO t_likes (id, post_id, praise)
VALUES (1, 1, 'Excellent explanation of Spring Boot concepts!'),
       (2, 1, 'Very helpful for beginners'),
       (3, 2, 'Great insights into JPA relationships'),
       (4, 2, 'Clear and concise explanation'),
       (5, 3, 'Really useful React Hooks tutorial'),
       (6, 3, 'Helped me understand hooks better'),
       (7, 4, 'Advanced concepts well explained'),
       (8, 4, 'Excellent TypeScript patterns'),
       (9, 5, 'Practical Docker best practices'),
       (10, 5, 'Very informative content'),
       (11, 6, 'Great microservices architecture overview'),
       (12, 6, 'Comprehensive explanation'),
       (13, 7, 'Insightful cloud native concepts'),
       (14, 7, 'Well-structured content'),
       (15, 8, 'Valuable API design principles'),
       (16, 8, 'Clear and practical examples'),
       (17, 9, 'Excellent testing strategies'),
       (18, 9, 'Very thorough coverage'),
       (19, 10, 'Helpful DevOps setup guide'),
       (20, 10, 'Clear step-by-step instructions');