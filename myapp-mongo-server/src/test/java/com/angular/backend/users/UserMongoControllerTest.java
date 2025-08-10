package com.angular.backend.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.angular.backend.AbstractMongoIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserMongoControllerTest extends AbstractMongoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMongoRepository userMongoRepository;

    @BeforeEach
    void setUp() {
        userMongoRepository.deleteAll();
    }

    @Test
    void testCreateAndGetUser() throws Exception {
        UserMongo user = new UserMongo();
        user.setFirstName("Bob");
        user.setEmail("bob@example.com");
        String json = objectMapper.writeValueAsString(user);
        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UserMongo created = objectMapper.readValue(response, UserMongo.class);
        mockMvc.perform(get("/users/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Bob"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        UserMongo user1 = new UserMongo();
        user1.setFirstName("A");
        userMongoRepository.save(user1);
        UserMongo user2 = new UserMongo();
        user2.setFirstName("B");
        userMongoRepository.save(user2);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[1].firstName").exists());
    }

    @Test
    void testUpdateUser() throws Exception {
        UserMongo user = new UserMongo();
        user.setFirstName("Old");
        UserMongo saved = userMongoRepository.save(user);
        saved.setFirstName("New");
        String json = objectMapper.writeValueAsString(saved);
        mockMvc.perform(put("/users/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    void testDeleteUser() throws Exception {
        UserMongo user = new UserMongo();
        user.setFirstName("ToDelete");
        UserMongo saved = userMongoRepository.save(user);
        mockMvc.perform(delete("/users/" + saved.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
