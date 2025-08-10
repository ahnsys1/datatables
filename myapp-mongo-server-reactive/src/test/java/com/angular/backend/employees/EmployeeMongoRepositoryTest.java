package com.angular.backend.employees;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.angular.backend.AbstractIntegrationTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataMongoTest
public class EmployeeMongoRepositoryTest extends AbstractIntegrationTest {

   
    @Autowired
    private EmployeeMongoRepository employeeMongoRepository;

    @BeforeEach
    void setUp() {
        employeeMongoRepository.deleteAll().block();
    }

    @Test
    void testCreateAndFindEmployee() {
        // Arrange
        EmployeeMongo employee = new EmployeeMongo("John Doe", "Developer", "123", "60000", "2023-01-15", "New York");

        // Act & Assert: Use StepVerifier for testing reactive streams
        Mono<EmployeeMongo> foundEmployee = employeeMongoRepository.save(employee)
                .flatMap(saved -> employeeMongoRepository.findById(saved.getId()));

        StepVerifier.create(foundEmployee)
                .expectNextMatches(e -> e.getName().equals("John Doe") && e.getId() != null)
                .verifyComplete();
    }
}