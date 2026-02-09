package io.github.biiiiiigmonster;

import io.github.biiiiiigmonster.driver.InMemoryDataDriver;
import io.github.biiiiiigmonster.entity.*;

/**
 * 测试数据初始化器
 * 用 Java 代码复现 db/data.sql 中的全部测试数据
 */
public class TestDataInitializer {

    public static void init(InMemoryDataDriver driver) {
        initUsers(driver);
        initProfiles(driver);
        initAddresses(driver);
        initPosts(driver);
        initPhones(driver);
        initVideos(driver);
        initRoles(driver);
        initTags(driver);
        initComments(driver);
        initImages(driver);
        initUserRoles(driver);
        initTaggables(driver);
        initLikes(driver);
    }

    private static void initUsers(InMemoryDataDriver driver) {
        String[][] data = {
                {"1", "John Doe", "john.doe@example.com"},
                {"2", "Jane Smith", "jane.smith@example.com"},
                {"3", "Michael Johnson", "michael.j@example.com"},
                {"4", "Emily Brown", "emily.b@example.com"},
                {"5", "David Wilson", "david.w@example.com"},
                {"6", "Sarah Davis", "sarah.d@example.com"},
                {"7", "James Miller", "james.m@example.com"},
                {"8", "Lisa Anderson", "lisa.a@example.com"},
                {"9", "Robert Taylor", "robert.t@example.com"},
                {"10", "Jennifer White", "jennifer.w@example.com"},
                {"11", "Michael Jordan", "michael.j@example.com"},
        };
        for (String[] row : data) {
            User u = new User();
            u.setId(Long.parseLong(row[0]));
            u.setName(row[1]);
            u.setEmail(row[2]);
            driver.put(User.class, u);
        }
    }

    private static void initProfiles(InMemoryDataDriver driver) {
        String[][] data = {
                {"1", "Software Engineer at Tech Corp", "1"},
                {"2", "Digital Marketing Specialist", "2"},
                {"3", "Product Manager", "3"},
                {"4", "UX Designer", "4"},
                {"5", "Data Scientist", "5"},
                {"6", "Content Creator", "6"},
                {"7", "Full Stack Developer", "7"},
                {"8", "Business Analyst", "8"},
                {"9", "Project Manager", "9"},
                {"10", "UI Designer", "10"},
        };
        for (String[] row : data) {
            Profile p = new Profile();
            p.setId(Long.parseLong(row[0]));
            p.setDescription(row[1]);
            p.setUserId(Long.parseLong(row[2]));
            driver.put(Profile.class, p);
        }
    }

    private static void initAddresses(InMemoryDataDriver driver) {
        String[][] data = {
                {"1", "1", "New York, NY"},
                {"2", "2", "San Francisco, CA"},
                {"3", "3", "Chicago, IL"},
                {"4", "4", "Los Angeles, CA"},
                {"5", "5", "Boston, MA"},
                {"6", "6", "Seattle, WA"},
                {"7", "7", "Austin, TX"},
                {"8", "8", "Portland, OR"},
                {"9", "9", "Miami, FL"},
                {"10", "10", "Denver, CO"},
        };
        for (String[] row : data) {
            Address a = new Address();
            a.setId(Long.parseLong(row[0]));
            a.setProfileId(Long.parseLong(row[1]));
            a.setLocation(row[2]);
            driver.put(Address.class, a);
        }
    }

    private static void initPosts(InMemoryDataDriver driver) {
        String[][] data = {
                {"1", "1", "Getting Started with Spring Boot"},
                {"2", "1", "Mastering JPA Relationships"},
                {"3", "2", "Introduction to React Hooks"},
                {"4", "2", "Advanced TypeScript Patterns"},
                {"5", "3", "Docker Best Practices"},
                {"6", "4", "Microservices Architecture"},
                {"7", "5", "Cloud Native Applications"},
                {"8", "6", "API Design Principles"},
                {"9", "7", "Testing Strategies"},
                {"10", "8", "DevOps Pipeline Setup"},
        };
        for (String[] row : data) {
            Post p = new Post();
            p.setId(Long.parseLong(row[0]));
            p.setUserId(Long.parseLong(row[1]));
            p.setTitle(row[2]);
            driver.put(Post.class, p);
        }
    }

    private static void initPhones(InMemoryDataDriver driver) {
        for (int i = 1; i <= 10; i++) {
            Phone p = new Phone();
            p.setId((long) i);
            p.setUserId((long) i);
            p.setNumber("1000" + i);
            driver.put(Phone.class, p);
        }
    }

    private static void initVideos(InMemoryDataDriver driver) {
        String[][] data = {
                {"1", "Spring Boot Tutorial", "https://example.com/videos/spring-boot"},
                {"2", "React Fundamentals", "https://example.com/videos/react"},
                {"3", "Docker Deep Dive", "https://example.com/videos/docker"},
                {"4", "Kubernetes Basics", "https://example.com/videos/kubernetes"},
                {"5", "AWS Services Overview", "https://example.com/videos/aws"},
                {"6", "MongoDB Tutorial", "https://example.com/videos/mongodb"},
                {"7", "GraphQL Introduction", "https://example.com/videos/graphql"},
                {"8", "Vue.js Essentials", "https://example.com/videos/vuejs"},
                {"9", "Python for Beginners", "https://example.com/videos/python"},
                {"10", "JavaScript ES6+", "https://example.com/videos/javascript"},
        };
        for (String[] row : data) {
            Video v = new Video();
            v.setId(Long.parseLong(row[0]));
            v.setTitle(row[1]);
            v.setUrl(row[2]);
            driver.put(Video.class, v);
        }
    }

    private static void initRoles(InMemoryDataDriver driver) {
        String[][] data = {
                {"1", "Administrator"}, {"2", "Moderator"}, {"3", "Editor"},
                {"4", "Author"}, {"5", "Contributor"}, {"6", "Reviewer"},
                {"7", "Developer"}, {"8", "Designer"}, {"9", "Tester"}, {"10", "Guest"},
        };
        for (String[] row : data) {
            Role r = new Role();
            r.setId(Long.parseLong(row[0]));
            r.setName(row[1]);
            driver.put(Role.class, r);
        }
    }

    private static void initTags(InMemoryDataDriver driver) {
        long[][] data = {
                {1, 0}, {2, 1}, {3, 0}, {4, 3}, {5, 0},
                {6, 5}, {7, 1}, {8, 2}, {9, 0}, {10, 6},
        };
        String[] names = {"Java", "Spring", "JavaScript", "React", "Docker", "Cloud", "Database", "Security", "Testing", "DevOps"};
        for (int i = 0; i < data.length; i++) {
            Tag t = new Tag();
            t.setId(data[i][0]);
            t.setName(names[i]);
            t.setParentId(data[i][1]);
            driver.put(Tag.class, t);
        }
    }

    private static void initComments(InMemoryDataDriver driver) {
        Object[][] data = {
                {1L, "Great tutorial!", 1L, "Post"},
                {2L, "Very helpful content", 1L, "Post"},
                {3L, "Nice explanation", 2L, "Post"},
                {4L, "Looking forward to more", 3L, "Post"},
                {5L, "Excellent video!", 1L, "Video"},
                {6L, "Well explained", 2L, "Video"},
                {7L, "This helped me a lot", 4L, "Post"},
                {8L, "Amazing content", 3L, "Video"},
                {9L, "Keep it up!", 5L, "Post"},
                {10L, "Very informative", 4L, "Video"},
        };
        for (Object[] row : data) {
            Comment c = new Comment();
            c.setId((Long) row[0]);
            c.setContent((String) row[1]);
            c.setCommentableId((Long) row[2]);
            c.setCommentableType((String) row[3]);
            driver.put(Comment.class, c);
        }
    }

    private static void initImages(InMemoryDataDriver driver) {
        Object[][] data = {
                {1L, "https://example.com/images/user1.jpg", 1L, "User"},
                {2L, "https://example.com/images/post1.jpg", 1L, "Post"},
                {3L, "https://example.com/images/user2.jpg", 2L, "User"},
                {4L, "https://example.com/images/post2.jpg", 2L, "Post"},
                {5L, "https://example.com/images/user3.jpg", 3L, "User"},
                {6L, "https://example.com/images/post3.jpg", 3L, "Post"},
                {7L, "https://example.com/images/user4.jpg", 4L, "User"},
                {8L, "https://example.com/images/post4.jpg", 4L, "Post"},
                {9L, "https://example.com/images/user5.jpg", 5L, "User"},
                {10L, "https://example.com/images/post5.jpg", 5L, "Post"},
        };
        for (Object[] row : data) {
            Image img = new Image();
            img.setId((Long) row[0]);
            img.setUrl((String) row[1]);
            img.setImageableId((Long) row[2]);
            img.setImageableType((String) row[3]);
            driver.put(Image.class, img);
        }
    }

    private static void initUserRoles(InMemoryDataDriver driver) {
        long[][] data = {
                {1, 1, 1}, {2, 1, 2}, {3, 1, 3}, {4, 2, 2}, {5, 2, 3},
                {6, 3, 3}, {7, 3, 4}, {8, 4, 4}, {9, 4, 5}, {10, 5, 1},
                {11, 5, 3}, {12, 6, 2}, {13, 6, 4}, {14, 7, 3}, {15, 7, 5},
                {16, 8, 4}, {17, 8, 5}, {18, 9, 1}, {19, 9, 2}, {20, 10, 2},
        };
        for (long[] row : data) {
            UserRole ur = new UserRole();
            ur.setId(row[0]);
            ur.setUserId(row[1]);
            ur.setRoleId(row[2]);
            driver.put(UserRole.class, ur);
        }
    }

    private static void initTaggables(InMemoryDataDriver driver) {
        Object[][] data = {
                {1L, 1L, 1L, "Post"}, {2L, 2L, 1L, "Post"}, {3L, 3L, 2L, "Post"}, {4L, 4L, 2L, "Post"},
                {5L, 5L, 3L, "Post"}, {6L, 6L, 3L, "Post"}, {7L, 7L, 4L, "Post"}, {8L, 8L, 4L, "Post"},
                {9L, 9L, 5L, "Post"}, {10L, 10L, 5L, "Post"},
                {11L, 1L, 1L, "Video"}, {12L, 2L, 1L, "Video"}, {13L, 3L, 2L, "Video"}, {14L, 4L, 2L, "Video"},
                {15L, 5L, 3L, "Video"}, {16L, 6L, 3L, "Video"}, {17L, 7L, 4L, "Video"}, {18L, 8L, 4L, "Video"},
                {19L, 9L, 5L, "Video"}, {20L, 10L, 5L, "Video"},
                {21L, 1L, 6L, "Post"}, {22L, 2L, 6L, "Post"}, {23L, 3L, 7L, "Post"}, {24L, 4L, 7L, "Post"},
                {25L, 5L, 8L, "Post"}, {26L, 6L, 8L, "Post"}, {27L, 7L, 9L, "Post"}, {28L, 8L, 9L, "Post"},
                {29L, 9L, 10L, "Post"}, {30L, 10L, 10L, "Post"},
                {31L, 1L, 6L, "Video"}, {32L, 2L, 6L, "Video"}, {33L, 3L, 7L, "Video"}, {34L, 4L, 7L, "Video"},
                {35L, 5L, 8L, "Video"}, {36L, 6L, 8L, "Video"}, {37L, 7L, 9L, "Video"}, {38L, 8L, 9L, "Video"},
                {39L, 9L, 10L, "Video"}, {40L, 10L, 10L, "Video"},
                {41L, 1L, 1L, "Phone"}, {42L, 2L, 1L, "Phone"}, {43L, 3L, 2L, "Phone"}, {44L, 4L, 2L, "Phone"},
                {45L, 5L, 3L, "Phone"},
        };
        for (Object[] row : data) {
            Taggable t = new Taggable();
            t.setId((Long) row[0]);
            t.setTagId((Long) row[1]);
            t.setTaggableId((Long) row[2]);
            t.setTaggableType((String) row[3]);
            driver.put(Taggable.class, t);
        }
    }

    private static void initLikes(InMemoryDataDriver driver) {
        Object[][] data = {
                {1L, 1L, "Excellent explanation of Spring Boot concepts!"},
                {2L, 1L, "Very helpful for beginners"},
                {3L, 2L, "Great insights into JPA relationships"},
                {4L, 2L, "Clear and concise explanation"},
                {5L, 3L, "Really useful React Hooks tutorial"},
                {6L, 3L, "Helped me understand hooks better"},
                {7L, 4L, "Advanced concepts well explained"},
                {8L, 4L, "Excellent TypeScript patterns"},
                {9L, 5L, "Practical Docker best practices"},
                {10L, 5L, "Very informative content"},
                {11L, 6L, "Great microservices architecture overview"},
                {12L, 6L, "Comprehensive explanation"},
                {13L, 7L, "Insightful cloud native concepts"},
                {14L, 7L, "Well-structured content"},
                {15L, 8L, "Valuable API design principles"},
                {16L, 8L, "Clear and practical examples"},
                {17L, 9L, "Excellent testing strategies"},
                {18L, 9L, "Very thorough coverage"},
                {19L, 10L, "Helpful DevOps setup guide"},
                {20L, 10L, "Clear step-by-step instructions"},
        };
        for (Object[] row : data) {
            Likes l = new Likes();
            l.setId((Long) row[0]);
            l.setPostId((Long) row[1]);
            l.setPraise((String) row[2]);
            driver.put(Likes.class, l);
        }
    }
}
