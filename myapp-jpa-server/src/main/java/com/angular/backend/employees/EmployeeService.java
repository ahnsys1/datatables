package com.angular.backend.employees;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TreeBuilder treeBuilder;

    public EmployeeService(EmployeeRepository employeeRepository, TreeBuilder treeBuilder) {
        this.employeeRepository = employeeRepository;
        this.treeBuilder = treeBuilder;
    }

    public List<EmployeeJPA> getEmployeeTree() {
        return treeBuilder.buildTree();
    }

    public List<EmployeeJPA> getPotentialManagers() {
        return employeeRepository.findPotentialManagers();
    }

    public EmployeeJPA createEmployee(EmployeeJPA employee, String managerId) {
        employee.setId(null);
        if (managerId != null && !managerId.isBlank()) {
            EmployeeJPA manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found with id: " + managerId));
            employee.setManager(manager);
        }
        EmployeeJPA savedEmployee = employeeRepository.save(employee);
        // The savedEmployee object has its ID. We can now fetch it again with its manager.
        return employeeRepository.findByIdWithManager(savedEmployee.getId());
    }

    public List<EmployeeJPA> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<EmployeeJPA> getAllEmployeesWithManagers() {
        return employeeRepository.findAllWithManagers();
    }

    public EmployeeJPA getEmployeeById(String id) {
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
        if (potentialNewManager == null) {
            return; // No new manager, no cycle is being introduced.
        }

        EmployeeJPA current = potentialNewManager;
        while (current != null) {
            if (current.equals(employee)) {
                // This means the employee is an ancestor of the potential new manager,
                // so setting this relationship would create a cycle.
                throw new IllegalArgumentException("Setting this manager would create a cycle in the employee hierarchy.");
            }
            current = current.getManager();
        }
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
        EmployeeJPA employeeToUpdate = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

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
            if (managerId.isBlank() || "null".equalsIgnoreCase(managerId)) {
                // A blank or "null" managerId indicates the manager should be removed.
                employeeToUpdate.setManager(null);
            } else {
                // An employee cannot be their own manager.
                if (id.equals(managerId)) {
                    throw new IllegalArgumentException("An employee cannot be their own manager.");
                }
                EmployeeJPA newManager = employeeRepository.findById(managerId)
                        .orElseThrow(() -> new EntityNotFoundException("Manager not found with id: " + managerId));
                detectCycle(employeeToUpdate, newManager); // Cycle detection
                employeeToUpdate.setManager(newManager);
            }
        }

        employeeRepository.save(employeeToUpdate);
        return employeeRepository.findByIdWithManager(id);
    }

    public boolean deleteEmployee(String id) {
        if (!employeeRepository.existsById(id)) {
            return false;
        }
        employeeRepository.deleteById(id);
        return true;
    }
}
