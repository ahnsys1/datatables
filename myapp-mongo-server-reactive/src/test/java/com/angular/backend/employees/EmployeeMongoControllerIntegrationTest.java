package com.angular.backend.employees;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.angular.backend.AbstractIntegrationTest;

import reactor.core.publisher.Mono;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient // Use this for reactive integration tests
 // Use this for reactive integration tests
public class EmployeeMongoControllerIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient; // Use WebTestClient, not MockMvc

    @Autowired
    private EmployeeMongoRepository employeeMongoRepository;

    @BeforeEach
    void setUp() {
        // Clean the repository before each test to ensure isolation
        employeeMongoRepository.deleteAll().block();
    }

    @Test
    void testGetAllEmployees() {
        // Arrange: Save some employees to the database
        EmployeeMongo employee1 = new EmployeeMongo("John Doe", "Developer", "123", "60000", "2023-01-15", "New York");
        EmployeeMongo employee2 = new EmployeeMongo("Jane Smith", "Manager", "456", "80000", "2022-05-20", "London");
        employeeMongoRepository.saveAll(List.of(employee1, employee2)).blockLast();

        // Act & Assert
        webTestClient.get().uri("/employees")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmployeeMongo.class)
                .hasSize(2);
    }

    @Test
    void testCreateEmployee() {
        // Arrange
        EmployeeMongo newEmployee = new EmployeeMongo("Peter Jones", "Analyst", "789", "70000", "2024-01-01", "Tokyo");

        // Act & Assert
        webTestClient.post().uri("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newEmployee), EmployeeMongo.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmployeeMongo.class)
                .value(createdEmployee -> {
                    assertThat(createdEmployee.getId()).isNotNull();
                    assertThat(createdEmployee.getName()).isEqualTo("Peter Jones");
                });
    }

    @Test
    void testGetEmployeeById_NotFound() {
        // Act & Assert
        webTestClient.get().uri("employees/{id}", "nonexistent-id")
                .exchange()
                .expectStatus().isNotFound();
    }
}