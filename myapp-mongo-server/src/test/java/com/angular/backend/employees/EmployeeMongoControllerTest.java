package com.angular.backend.employees;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
    void testCreateEmployeeWithManager() throws Exception {
        EmployeeMongo manager = new EmployeeMongo();
        manager.setName("Manager");
        EmployeeMongo savedManager = employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Subordinate");

        mockMvc.perform(post("/employees")
                .param("managerId", savedManager.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Subordinate"))
                .andExpect(jsonPath("$.managerId").value(savedManager.getId()));
    }

    @Test
    void testCreateEmployeeWithInvalidManagerFails() throws Exception {
        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Subordinate");

        // Note: This expects a 500 error because no specific exception handler is
        // configured for the RuntimeException thrown by the service.
        mockMvc.perform(post("/employees")
                .param("managerId", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isInternalServerError());
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
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("A", "B")));
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
    void testUpdateEmployeeWithManager() throws Exception {
        EmployeeMongo manager = new EmployeeMongo();
        manager.setName("Manager");
        employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Employee");
        employeeMongoRepository.save(employee);

        employee.setName("Updated Name");

        mockMvc.perform(put("/employees/{id}", employee.getId())
                .param("managerId", manager.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.managerId").value(manager.getId()));
    }

    @Test
    void testUpdateEmployeeToRemoveManager() throws Exception {
        EmployeeMongo manager = new EmployeeMongo();
        manager.setName("Manager");
        employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Employee");
        employee.setManagerId(manager.getId());
        employeeMongoRepository.save(employee);

        mockMvc.perform(put("/employees/{id}", employee.getId())
                .param("managerId", "") // remove manager
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerId").doesNotExist());
    }

    @Test
    void testUpdateEmployee_setSelfAsManager_Fails() throws Exception {
        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Employee");
        employeeMongoRepository.save(employee);

        mockMvc.perform(put("/employees/{id}", employee.getId())
                .param("managerId", employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testUpdateEmployee_createCycle_Fails() throws Exception {
        EmployeeMongo ceo = new EmployeeMongo();
        ceo.setName("CEO");
        employeeMongoRepository.save(ceo);

        EmployeeMongo manager = new EmployeeMongo();
        manager.setName("Manager");
        manager.setManagerId(ceo.getId());
        employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Employee");
        employee.setManagerId(manager.getId());
        employeeMongoRepository.save(employee);

        // Try to make CEO report to employee
        mockMvc.perform(put("/employees/{id}", ceo.getId())
                .param("managerId", employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ceo)))
                .andExpect(status().is4xxClientError());
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

    @Test
    void testGetEmployeeTree() throws Exception {
        EmployeeMongo root1 = new EmployeeMongo();
        root1.setName("Root 1");
        employeeMongoRepository.save(root1);

        EmployeeMongo child1 = new EmployeeMongo();
        child1.setName("Child 1");
        child1.setManagerId(root1.getId());
        employeeMongoRepository.save(child1);

        mockMvc.perform(get("/employees/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Root 1")))
                .andExpect(jsonPath("$[0].children", hasSize(1)))
                .andExpect(jsonPath("$[0].children[0].name", is("Child 1")));
    }

    @Test
    void testGetPotentialManagers() throws Exception {
        EmployeeMongo root = new EmployeeMongo();
        root.setName("Root");
        employeeMongoRepository.save(root);

        EmployeeMongo sub = new EmployeeMongo();
        sub.setName("Sub");
        sub.setManagerId(root.getId());
        employeeMongoRepository.save(sub);

        EmployeeMongo subWithRights = new EmployeeMongo();
        subWithRights.setName("Sub with Rights");
        subWithRights.setManagerId(root.getId());
        subWithRights.setHasManagerRights(true);
        employeeMongoRepository.save(subWithRights);

        mockMvc.perform(get("/employees/managers"))
                .andExpect(status().isOk());
        //           .andExpect(jsonPath("$", hasSize(2)))
        //.andExpect(jsonPath("$[*].name", containsInAnyOrder("Root", "Sub with Rights")));
    }

    @Test
    void testGetAllEmployeesWithManagers() throws Exception {
        EmployeeMongo manager = new EmployeeMongo();
        manager.setName("Manager");
        employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Employee");
        employee.setManagerId(manager.getId());
        employeeMongoRepository.save(employee);

        String s = mockMvc.perform(get("/employees/with-managers")).andReturn().getResponse().getContentAsString();
        System.err.println(s);
        mockMvc.perform(get("/employees/with-managers"))
                .andExpect(status().isOk())
                //           .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.name == 'Employee')].manager.name", contains("Manager")));
    }
}
