package com.angular.backend.employees;

import java.time.LocalDate;

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
public class EmployeeMongoControllerTest extends AbstractMongoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeMongoRepository employeeMongoRepository;

    @BeforeEach
    void setUp() {
        employeeMongoRepository.deleteAll();
    }

    @Test
    void testCreateAndGetEmployee() throws Exception {
        EmployeeMongo emp = new EmployeeMongo();
        emp.setName("Alice");
        emp.setPosition("Dev");
        emp.setSalary("1000");
        emp.setOffice("NY");
        emp.setExtn("123");
        emp.setStart_date(LocalDate.parse("2024-01-01"));
        String json = objectMapper.writeValueAsString(emp);
        String response = mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        EmployeeMongo created = objectMapper.readValue(response, EmployeeMongo.class);
        mockMvc.perform(get("/employees/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void testGetAllEmployees() throws Exception {
        EmployeeMongo emp1 = new EmployeeMongo();
        emp1.setName("A");
        employeeMongoRepository.save(emp1);
        EmployeeMongo emp2 = new EmployeeMongo();
        emp2.setName("B");
        employeeMongoRepository.save(emp2);
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void testUpdateEmployee() throws Exception {
        EmployeeMongo emp = new EmployeeMongo();
        emp.setName("Old");
        EmployeeMongo saved = employeeMongoRepository.save(emp);
        saved.setName("New");
        String json = objectMapper.writeValueAsString(saved);
        mockMvc.perform(put("/employees/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        EmployeeMongo emp = new EmployeeMongo();
        emp.setName("ToDelete");
        EmployeeMongo saved = employeeMongoRepository.save(emp);
        mockMvc.perform(delete("/employees/" + saved.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/employees/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
