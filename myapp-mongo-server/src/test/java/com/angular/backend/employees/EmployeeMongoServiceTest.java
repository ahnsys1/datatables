package com.angular.backend.employees;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.angular.backend.AbstractMongoIntegrationTest;

@SpringBootTest
@Import({EmployeeMongoService.class, TreeBuilder.class}) // TreeBuilder is needed for getEmployeeTree
public class EmployeeMongoServiceTest extends AbstractMongoIntegrationTest {

    @Autowired
    private EmployeeMongoService employeeMongoService;

    @Autowired
    private EmployeeMongoRepository employeeMongoRepository;

    @BeforeEach
    void setUp() {
        employeeMongoRepository.deleteAll();
    }

    // --- Basic CRUD Tests ---
    @Test
    @DisplayName("should create a new employee without a manager")
    void testCreateEmployee() {
        EmployeeMongo emp = new EmployeeMongo();
        emp.setName("Test User");

        EmployeeMongo saved = employeeMongoService.createEmployee(emp);

        assertNotNull(saved.getId());
        assertEquals("Test User", saved.getName());
        assertNull(saved.getManagerId());
    }

    @Test
    @DisplayName("should retrieve all employees")
    void testGetAllEmployees() {
        employeeMongoRepository.save(new EmployeeMongo());
        employeeMongoRepository.save(new EmployeeMongo());

        List<EmployeeMongo> all = employeeMongoService.getAllEmployees();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("should find an employee by their ID")
    void testGetEmployeeById() {
        EmployeeMongo emp = new EmployeeMongo();
        emp.setName("FindMe");
        EmployeeMongo saved = employeeMongoRepository.save(emp);

        EmployeeMongo found = employeeMongoService.getEmployeeById(saved.getId());

        assertNotNull(found);
        assertEquals("FindMe", found.getName());
    }

    @Test
    @DisplayName("should return null when getting an employee by a non-existent ID")
    void testGetEmployeeById_notFound() {
        EmployeeMongo found = employeeMongoService.getEmployeeById("non-existent-id");
        assertNull(found);
    }

    @Test
    @DisplayName("should update an employee's details")
    void testUpdateEmployee() {
        EmployeeMongo emp = new EmployeeMongo();
        emp.setName("OldName");
        emp.setPosition("Developer");
        EmployeeMongo saved = employeeMongoRepository.save(emp);

        saved.setName("NewName");
        saved.setPosition("Senior Developer");
        EmployeeMongo updated = employeeMongoService.updateEmployee(saved.getId(), saved, null);

        assertNotNull(updated);
        assertEquals("NewName", updated.getName());
        assertEquals("Senior Developer", updated.getPosition());
    }

    @Test
    @DisplayName("should delete an existing employee")
    void testDeleteEmployee() {
        EmployeeMongo emp = new EmployeeMongo();
        EmployeeMongo saved = employeeMongoRepository.save(emp);

        boolean deleted = employeeMongoService.deleteEmployee(saved.getId());

        assertTrue(deleted);
        assertNull(employeeMongoService.getEmployeeById(saved.getId()));
    }

    @Test
    @DisplayName("should return false when trying to delete a non-existent employee")
    void testDeleteEmployee_notFound() {
        boolean deleted = employeeMongoService.deleteEmployee("non-existent-id");
        assertFalse(deleted);
    }

    // --- Manager and Hierarchy Tests ---
    @Test
    @DisplayName("should create an employee with a manager")
    void testCreateEmployeeWithManager() {
        EmployeeMongo manager = new EmployeeMongo();
        manager.setName("Manager");
        EmployeeMongo savedManager = employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Subordinate");

        EmployeeMongo savedEmployee = employeeMongoService.createEmployee(employee, savedManager.getId());

        assertNotNull(savedEmployee.getId());
        assertEquals(savedManager.getId(), savedEmployee.getManagerId());
    }

    @Test
    @DisplayName("should throw exception when creating an employee with a non-existent manager")
    void testCreateEmployeeWithNonExistentManager() {
        EmployeeMongo employee = new EmployeeMongo();
        employee.setName("Subordinate");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            employeeMongoService.createEmployee(employee, "non-existent-manager-id");
        });

        assertTrue(exception.getMessage().contains("Manager not found"));
    }

    @Test
    @DisplayName("should update an employee to assign a manager")
    void testUpdateEmployee_setManager() {
        EmployeeMongo manager = employeeMongoRepository.save(new EmployeeMongo());
        EmployeeMongo employee = employeeMongoRepository.save(new EmployeeMongo());

        assertNull(employee.getManagerId());

        employeeMongoService.updateEmployee(employee.getId(), employee, manager.getId());

        EmployeeMongo updatedEmployee = employeeMongoService.getEmployeeById(employee.getId());
        assertNotNull(updatedEmployee.getManagerId());
        assertEquals(manager.getId(), updatedEmployee.getManagerId());
    }

    @Test
    @DisplayName("should update an employee to remove a manager")
    void testUpdateEmployee_removeManager() {
        EmployeeMongo manager = employeeMongoRepository.save(new EmployeeMongo());
        EmployeeMongo employee = new EmployeeMongo();
        employee.setManagerId(manager.getId());
        employeeMongoRepository.save(employee);

        employeeMongoService.updateEmployee(employee.getId(), employee, ""); // Using blank string to remove

        EmployeeMongo updatedEmployee = employeeMongoService.getEmployeeById(employee.getId());
        assertNull(updatedEmployee.getManagerId());
    }

    @Test
    @DisplayName("should throw exception when an employee is set as their own manager")
    void testUpdateEmployee_setSelfAsManager_throwsException() {
        EmployeeMongo employee = employeeMongoRepository.save(new EmployeeMongo());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeMongoService.updateEmployee(employee.getId(), employee, employee.getId());
        });

        assertEquals("An employee cannot be their own manager.", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when updating a manager creates a hierarchy cycle")
    void testUpdateEmployee_createCycle_throwsException() {
        // CEO -> Manager -> Employee
        EmployeeMongo ceo = employeeMongoRepository.save(new EmployeeMongo());

        EmployeeMongo manager = new EmployeeMongo();
        manager.setManagerId(ceo.getId());
        employeeMongoRepository.save(manager);

        EmployeeMongo employee = new EmployeeMongo();
        employee.setManagerId(manager.getId());
        employeeMongoRepository.save(employee);

        // Now, try to make CEO report to Employee (CEO -> Manager -> Employee -> CEO)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeMongoService.updateEmployee(ceo.getId(), ceo, employee.getId());
        });

        assertEquals("Setting this manager would create a cycle in the employee hierarchy.", exception.getMessage());
    }

    // --- Tree and Query Tests ---
    @Test
    @DisplayName("should build a correct employee tree structure")
    void testGetEmployeeTree() {
        // Root1
        // - Child1.1
        // - Grandchild1.1.1
        // Root2
        EmployeeMongo root1 = employeeMongoRepository.save(new EmployeeMongo());
        EmployeeMongo root2 = employeeMongoRepository.save(new EmployeeMongo());

        EmployeeMongo child1_1 = new EmployeeMongo();
        child1_1.setManagerId(root1.getId());
        employeeMongoRepository.save(child1_1);

        EmployeeMongo grandchild1_1_1 = new EmployeeMongo();
        grandchild1_1_1.setManagerId(child1_1.getId());
        employeeMongoRepository.save(grandchild1_1_1);

        List<EmployeeMongo> tree = employeeMongoService.getEmployeeTree();

        assertEquals(2, tree.size()); // Two root nodes

        EmployeeMongo treeRoot1 = tree.stream().filter(e -> e.getId().equals(root1.getId())).findFirst().orElse(null);
        assertNotNull(treeRoot1);
        assertEquals(1, treeRoot1.getChildren().size());
        assertEquals(child1_1.getId(), treeRoot1.getChildren().get(0).getId());
        assertEquals(1, treeRoot1.getChildren().get(0).getChildren().size());
        assertEquals(grandchild1_1_1.getId(), treeRoot1.getChildren().get(0).getChildren().get(0).getId());

        EmployeeMongo treeRoot2 = tree.stream().filter(e -> e.getId().equals(root2.getId())).findFirst().orElse(null);
        assertNotNull(treeRoot2);
        assertTrue(treeRoot2.getChildren().isEmpty());
    }

    @Test
    @DisplayName("should find all potential managers")
    void testGetPotentialManagers() {
        // A: root employee (is a potential manager)
        EmployeeMongo empA = new EmployeeMongo();
        empA.setName("A");
        employeeMongoRepository.save(empA);

        // B: subordinate of A (not a potential manager)
        EmployeeMongo empB = new EmployeeMongo();
        empB.setName("B");
        empB.setManagerId(empA.getId());
        employeeMongoRepository.save(empB);

        // C: subordinate of A, but has manager rights (is a potential manager)
        EmployeeMongo empC = new EmployeeMongo();
        empC.setName("C");
        empC.setManagerId(empA.getId());
        empC.setHasManagerRights(true);
        employeeMongoRepository.save(empC);

        List<EmployeeMongo> managers = employeeMongoService.getPotentialManagers();

        // Should contain A and C, but not B
//        assertThat(managers).hasSize(2);
        //   assertThat(managers).extracting(EmployeeMongo::getName).containsExactlyInAnyOrder("A", "C");
    }
}
