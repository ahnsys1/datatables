package com.angular.backend.employees;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final TreeBuilder treeBuilder;
    private final RabbitTemplate rabbitTemplate;

    public EmployeeService(EmployeeRepository employeeRepository, TreeBuilder treeBuilder,
            RabbitTemplate rabbitTemplate) {
        this.employeeRepository = employeeRepository;
        this.treeBuilder = treeBuilder;
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean existsEmployeeByName(String name) {
        log.info("Checking if employee exists by name: {}", name);
        boolean exists = employeeRepository.existsByName(name);
        log.info("Employee with name '{}' exists: {}", name, exists);
        return exists;
    }

    public List<EmployeeJPA> getEmployeeTree() {
        log.info("Building employee tree");
        return treeBuilder.buildTree();
    }

    public List<EmployeeJPA> getPotentialManagers() {
        log.info("Fetching potential managers");
        List<EmployeeJPA> managers = employeeRepository.findPotentialManagers();
        log.info("Found {} potential managers", managers.size());
        return managers;
    }

    public EmployeeJPA createEmployee(EmployeeJPA employee) {
        log.info("Creating employee with no specified manager");
        return createEmployee(employee, null);
    }

    public EmployeeJPA createEmployee(EmployeeJPA employee, String managerId) {
        log.info("Attempting to create new employee '{}' with manager ID: {}", employee.getName(), managerId);
        employee.setId(null);
        if (managerId != null && !managerId.isBlank()) {
            log.debug("Finding manager with ID: {}", managerId);
            EmployeeJPA manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found with id: " + managerId));
            employee.setManager(manager);
            log.debug("Assigned manager '{}' to new employee", manager.getName());
        }
        EmployeeJPA savedEmployee = employeeRepository.save(employee);
        log.info("Successfully created employee with ID: {}", savedEmployee.getId());
        // Fetch the employee with manager to ensure all details are present for the message
        EmployeeJPA employeeWithManager = employeeRepository.findByIdWithManager(savedEmployee.getId());
        // Send to the topic exchange with the 'new' routing key
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NEW,
                employeeWithManager);
        return employeeWithManager;
    }

    public List<EmployeeJPA> getAllEmployees() {
        log.info("Fetching all employees");
        List<EmployeeJPA> employees = employeeRepository.findAllWithManagers();
        log.info("Found {} employees", employees.size());
        return employees;
    }

    public List<EmployeeJPA> getAllEmployeesWithManagers() {
        log.info("Fetching all employees with their managers");
        List<EmployeeJPA> employees = employeeRepository.findAllWithManagers();
        log.info("Found {} employees with manager data", employees.size());
        return employees;
    }

    public EmployeeJPA getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);
        return employeeRepository.findById(id).orElse(null);
    }

    /**
     * Checks if setting a new manager for an employee would create a cycle in
     * the hierarchy. A cycle occurs if the employee is an ancestor of their
     * proposed new manager.
     *
     * @param employee The employee being updated.
     * @param potentialNewManager The proposed new manager.
     * @throws IllegalArgumentException if a cycle is detected.
     */
    private void detectCycle(EmployeeJPA employee, EmployeeJPA potentialNewManager) {
        log.debug("Detecting cycle for employee ID {} with potential new manager ID {}", employee.getId(), potentialNewManager != null ? potentialNewManager.getId() : "null");
        if (potentialNewManager == null) {
            return; // No new manager, no cycle is being introduced.
        }

        EmployeeJPA backupManager = employee.getManager();
        employee.setManager(potentialNewManager);

        EmployeeJPA current = potentialNewManager;
        while (current != null) {
            if (current.equals(employee)) {
                // This means the employee is an ancestor of the potential new manager,
                // so setting this relationship would create a cycle.
                employee.setManager(backupManager);
                log.warn("Cycle detected: Employee {} is an ancestor of potential manager {}", employee.getId(), potentialNewManager.getId());
                throw new IllegalArgumentException("Setting this manager would create a cycle in the employee hierarchy.");
            }
            current = current.getManager();
        }

        employee.setManager(backupManager);
    }

    private boolean isDescendantOf(EmployeeJPA possibleDescendant, EmployeeJPA possibleAncestor) {
        EmployeeJPA current = possibleDescendant;
        while (current != null) {
            if (current.equals(possibleAncestor)) {
                return true;
            }
            current = current.getManager();
        }
        return false;
    }

    public List<EmployeeJPA> moveChildrenToManager(String employeeId, String managerId) {
        log.info("Moving children of employee {} to manager {}", employeeId, managerId);
        EmployeeJPA employee = employeeRepository.findByIdWithManager(employeeId);
        if (employee == null) {
            throw new EntityNotFoundException("Employee not found with id: " + employeeId);
        }

        EmployeeJPA newManager = employeeRepository.findByIdWithManager(managerId);
        if (newManager == null) {
            throw new EntityNotFoundException("Manager not found with id: " + managerId);
        }

        if (employee.equals(newManager)) {
            throw new IllegalArgumentException("Selected employee cannot receive their own children.");
        }

        if (isDescendantOf(newManager, employee)) {
            throw new IllegalArgumentException("New manager cannot be a child of the selected employee.");
        }

        List<EmployeeJPA> children = employeeRepository.findByManagerId(employeeId);
        for (EmployeeJPA child : children) {
            detectCycle(child, newManager);
            child.setManager(newManager);
        }

        List<EmployeeJPA> savedChildren = employeeRepository.saveAll(children);
        for (EmployeeJPA child : savedChildren) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_UPDATED, child);
        }
        log.info("Moved {} children from employee {} to manager {}", savedChildren.size(), employeeId, managerId);
        return savedChildren;
    }

    /**
     * Updates an existing employee's details.
     *
     * @param id The ID of the employee to update.
     * @param employeeDetails An EmployeeJPA object containing the new details.
     * @param managerId The ID of the new manager. If null, the manager is not
     * changed. If blank or "null", the manager is removed.
     * @return The updated EmployeeJPA object, or null if no employee was found
     * with the given ID.
     */
    public EmployeeJPA updateEmployee(String id, EmployeeJPA employeeDetails, String managerId) {
        log.info("Attempting to update employee with ID: {}", id);
        EmployeeJPA employeeToUpdate = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
        log.debug("Found employee to update: {}", employeeToUpdate.getName());

        // Update fields from the incoming employee object
        employeeToUpdate.setName(employeeDetails.getName());
        employeeToUpdate.setPosition(employeeDetails.getPosition());
        employeeToUpdate.setSalary(employeeDetails.getSalary());
        employeeToUpdate.setStart_date(employeeDetails.getStart_date());
        employeeToUpdate.setOffice(employeeDetails.getOffice());
        employeeToUpdate.setExtn(employeeDetails.getExtn());
        employeeToUpdate.setHasManagerRights(employeeDetails.isHasManagerRights());

        // Only update the manager if a managerId is provided in the request.
        // If managerId is null, the existing manager is preserved.
        if (managerId != null) {
            log.debug("Manager ID provided for update: {}", managerId);
            if (managerId.isBlank() || "null".equalsIgnoreCase(managerId)) {
                // A blank or "null" managerId indicates the manager should be removed.
                log.info("Removing manager from employee {}", id);
                employeeToUpdate.setManager(null);
            } else {
                // An employee cannot be their own manager.
                if (id.equals(managerId)) {
                    log.warn("Attempt to make employee {} their own manager", id);
                    throw new IllegalArgumentException("An employee cannot be their own manager.");
                }
                log.debug("Finding new manager with ID: {}", managerId);
                EmployeeJPA newManager = employeeRepository.findById(managerId)
                        .orElseThrow(() -> new EntityNotFoundException("Manager not found with id: " + managerId));
                detectCycle(employeeToUpdate, newManager); // Cycle detection
                employeeToUpdate.setManager(newManager);
                log.info("Set new manager for employee {} to {}", id, newManager.getId());
            }
        }

        employeeRepository.save(employeeToUpdate);
        log.debug("");
        // Send the entire object. RabbitTemplate will convert it to JSON.
        // Send to the topic exchange with the 'updated' routing key
        EmployeeJPA employeeWithManager = employeeRepository.findByIdWithManager(id);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_UPDATED,
                employeeWithManager);
        log.info("Successfully updated employee with ID: {}", id);
        return employeeWithManager;
    }

    public EmployeeJPA findByIdWithManager(String id) {
        log.info("Fetching employee by ID with manager: {}", id);
        EmployeeJPA employee = employeeRepository.findByIdWithManager(id);
        if (employee == null) {
            log.warn("Employee with ID {} not found.", id);
        }

        return employee;
    }

    public boolean deleteEmployee(String id) {
        if (!employeeRepository.existsById(id)) {
            return false;
        }

        if (employeeRepository.existsByManagerId(id)) {
            log.warn("Attempted to delete employee {} who is a manager.", id);
            throw new IllegalStateException("Cannot delete employee who is a manager with subordinates.");
        }

        employeeRepository.deleteById(id);
        log.info("Successfully deleted employee with ID: {}", id);
        return true;
    }

}
