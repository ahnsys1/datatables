package com.angular.backend.employees;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.angular.backend.AbstractPostgresIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
public class EmployeeJPAControllerTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void createEmployee_shouldReturnCreated() throws Exception {
        EmployeeJPA employee = new EmployeeJPA();
        employee.setName("John Doe");
        employee.setPosition("Developer");
        employee.setStart_date(LocalDate.of(2023, 1, 15));

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.position", is("Developer")))
                .andExpect(jsonPath("$.start_date", is(employee.getStart_date().toString())));
    }

    @Test
    void getAllEmployees_shouldReturnOk() throws Exception {
        EmployeeJPA employee1 = new EmployeeJPA();
        employee1.setName("John Doe");
        EmployeeJPA employee2 = new EmployeeJPA();
        employee2.setName("Jane Smith");
        employeeRepository.saveAll(Arrays.asList(employee1, employee2));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));
    }

    @Test
    void getEmployeeById_whenExists_shouldReturnOk() throws Exception {
        EmployeeJPA employee = new EmployeeJPA();
        employee.setName("Find Me");
        EmployeeJPA savedEmployee = employeeRepository.save(employee);

        mockMvc.perform(get("/employees/{id}", savedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedEmployee.getId())))
                .andExpect(jsonPath("$.name", is("Find Me")));
    }

    @Test
    void getEmployeeById_whenNotExists_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/employees/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_whenExists_shouldReturnNoContent() throws Exception {
        EmployeeJPA employee = new EmployeeJPA();
        employee.setName("Delete Me");
        EmployeeJPA savedEmployee = employeeRepository.save(employee);

        mockMvc.perform(delete("/employees/{id}", savedEmployee.getId()))
                .andExpect(status().isNoContent());

        assertFalse(employeeRepository.existsById(savedEmployee.getId()));
    }

    @Test
    void deleteEmployee_whenNotExists_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/employees/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

}
