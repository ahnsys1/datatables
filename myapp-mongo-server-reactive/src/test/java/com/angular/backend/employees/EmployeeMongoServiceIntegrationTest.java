package com.angular.backend.employees;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.angular.backend.AbstractIntegrationTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EmployeeMongoServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeMongoService employeeMongoService;

    @Autowired
    private EmployeeMongoRepository employeeMongoRepository;

    private EmployeeMongo employee1;
    private EmployeeMongo employee2;

    @BeforeEach
    void setUp() {
        employeeMongoRepository.deleteAll().block();

        employee1 = new EmployeeMongo();
        employee1.setId(UUID.randomUUID().toString());
        employee1.setName("John Doe");
        employee1.setPosition("Developer");
        employee1.setSalary("90000");
        employee1.setOffice("New York");
        employee1.setExtn("1234");
        employee1.setStart_date("2023-01-15");

        employee2 = new EmployeeMongo();
        employee2.setId(UUID.randomUUID().toString());
        employee2.setName("Jane Smith");
        employee2.setPosition("Manager");
        employee2.setSalary("120000");
        employee2.setOffice("London");
        employee2.setExtn("5678");
        employee2.setStart_date("2022-05-20");
    }

    @AfterEach
    void tearDown() {
        employeeMongoRepository.deleteAll().block();
    }

    @Test
    void testCreateEmployee() {
        // when
        EmployeeMongo createdEmployee = employeeMongoService.createEmployee(employee1).block();

        // then
        assertNotNull(createdEmployee);
        assertNotNull(createdEmployee.getId());
        assertEquals("John Doe", createdEmployee.getName());
        assertEquals(1L, employeeMongoRepository.count().block());
    }

    @Test
    void testGetAllEmployees() {
        // given
        employeeMongoService.createEmployee(employee1).block();
        employeeMongoService.createEmployee(employee2).block();

        // when
        List<EmployeeMongo> employees = employeeMongoService.getAllEmployees().collectList().block();

        // then
        assertNotNull(employees);
        assertEquals(2, employees.size());
    }

    @Test
    void testGetEmployeeById() {
        // given
        EmployeeMongo savedEmployee = employeeMongoService.createEmployee(employee1).block();
        assertNotNull(savedEmployee);

        // when
        EmployeeMongo foundEmployee = employeeMongoService.getEmployeeById(savedEmployee.getId()).block();

        // then
        assertNotNull(foundEmployee);
        assertEquals(savedEmployee.getId(), foundEmployee.getId());
        assertEquals("John Doe", foundEmployee.getName());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        // when
        EmployeeMongo foundEmployee = employeeMongoService.getEmployeeById(UUID.randomUUID().toString()).blockOptional().orElse(null);

        // then
        assertNull(foundEmployee);
    }

    @Test
    void testUpdateEmployee() {
        // given
        EmployeeMongo savedEmployee = employeeMongoService.createEmployee(employee1).block();
        assertNotNull(savedEmployee);
        savedEmployee.setPosition("Senior Developer");

        // when
        EmployeeMongo updatedEmployee = employeeMongoService.updateEmployee(savedEmployee.getId(), savedEmployee).block();

        // then
        assertNotNull(updatedEmployee);
        assertEquals("Senior Developer", updatedEmployee.getPosition());

        EmployeeMongo foundAfterUpdate = employeeMongoService.getEmployeeById(savedEmployee.getId()).block();
        assertNotNull(foundAfterUpdate);
        assertEquals("Senior Developer", foundAfterUpdate.getPosition());
    }

    @Test
    void testUpdateEmployee_NotFound() {
        // when
        EmployeeMongo updatedEmployee = employeeMongoService.updateEmployee(UUID.randomUUID().toString(), employee1).blockOptional().orElse(null);

        // then
        assertNull(updatedEmployee);
    }

    @Test
    void testDeleteEmployee() {
        // given
        EmployeeMongo savedEmployee = employeeMongoService.createEmployee(employee1).block();
        assertNotNull(savedEmployee);
        String employeeId = savedEmployee.getId();

        // when
        employeeMongoService.deleteEmployee(employeeId).block();

        // then
        Optional<EmployeeMongo> foundEmployee = employeeMongoService.getEmployeeById(employeeId).blockOptional();
        assertTrue(foundEmployee.isEmpty());
        assertEquals(0L, employeeMongoRepository.count().block());
    }

    @Test
    void testDeleteEmployee_NotFound() {
        // when
        employeeMongoService.deleteEmployee(UUID.randomUUID().toString()).block();
        // No exception should be thrown, delete is idempotent
    }
}