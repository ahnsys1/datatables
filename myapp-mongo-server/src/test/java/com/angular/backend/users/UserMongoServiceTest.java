package com.angular.backend.users;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.angular.backend.AbstractMongoIntegrationTest;

@SpringBootTest
@Testcontainers
@Import(UserMongoService.class)
public class UserMongoServiceTest extends AbstractMongoIntegrationTest {

    @Autowired
    private UserMongoRepository userMongoRepository;

    @Autowired
    private UserMongoService userMongoService;

    @BeforeEach
    void setUp() {
        userMongoRepository.deleteAll();
    }

    @Test
    void testCreateAndGetUser() {
        UserMongo user = new UserMongo();
        user.setFirstName("Bob");
        user.setEmail("bob@example.com");
        UserMongo saved = userMongoService.createUser(user);
        assertNotNull(saved.getId());
        UserMongo found = userMongoService.getUserById(saved.getId());
        assertEquals("Bob", found.getFirstName());
    }

    @Test
    void testGetAllUsers() {
        UserMongo user1 = new UserMongo();
        user1.setFirstName("A");
        userMongoService.createUser(user1);
        UserMongo user2 = new UserMongo();
        user2.setFirstName("B");
        userMongoService.createUser(user2);
        List<UserMongo> all = userMongoService.getAllUsers();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdateUser() {
        UserMongo user = new UserMongo();
        user.setFirstName("Old");
        UserMongo saved = userMongoService.createUser(user);
        saved.setFirstName("New");
        UserMongo updated = userMongoService.updateUser(saved.getId(), saved);
        assertEquals("New", updated.getFirstName());
    }

    @Test
    void testDeleteUser() {
        UserMongo user = new UserMongo();
        user.setFirstName("ToDelete");
        UserMongo saved = userMongoService.createUser(user);
        boolean deleted = userMongoService.deleteUser(saved.getId());
        assertTrue(deleted);
        assertNull(userMongoService.getUserById(saved.getId()));
    }
}
