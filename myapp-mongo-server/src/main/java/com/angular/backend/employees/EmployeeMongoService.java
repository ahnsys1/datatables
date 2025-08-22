package com.angular.backend.employees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeMongoService {

    @Autowired
    private EmployeeMongoRepository employeeMongoRepository;

    @Autowired
    private TreeBuilder treeBuilder;

    public List<EmployeeMongo> getEmployeeTree() {
        return treeBuilder.buildTree();
    }

    public List<EmployeeMongo> getPotentialManagers() {
        return employeeMongoRepository.findPotentialManagers();
    }

    public EmployeeMongo createEmployee(EmployeeMongo employee) {
        employee.setId(null);
        return employeeMongoRepository.save(employee);
    }

    public EmployeeMongo createEmployee(EmployeeMongo employee, String managerId) {
        employee.setId(null);
        if (managerId != null && !managerId.isBlank()) {
            // Just check if manager exists. We only store the ID.
            employeeMongoRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));
            employee.setManagerId(managerId);
        }
        return employeeMongoRepository.save(employee);
    }

    public List<EmployeeMongo> getAllEmployees() {
        return employeeMongoRepository.findAll();
    }

    public EmployeeMongo getEmployeeById(String id) {
        return employeeMongoRepository.findById(id).orElse(null);
    }

    private void detectCycle(EmployeeMongo employee, EmployeeMongo potentialNewManager) {
        if (potentialNewManager == null) {
            return;
        }

        EmployeeMongo current = potentialNewManager;
        while (current != null) {
            if (current.equals(employee)) {
                throw new IllegalArgumentException("Setting this manager would create a cycle in the employee hierarchy.");
            }
            if (current.getManagerId() == null) {
                current = null;
            } else {
                // Fetch the next manager in the hierarchy.
                // .orElse(null) is important in case a manager in the chain is deleted.
                current = employeeMongoRepository.findById(current.getManagerId()).orElse(null);
            }
        }
    }

    public EmployeeMongo updateEmployee(String id, EmployeeMongo employeeDetails, String managerId) {
        Optional<EmployeeMongo> existingEmployeeOpt = employeeMongoRepository.findById(id);
        if (existingEmployeeOpt.isEmpty()) {
            return null;
        }
        EmployeeMongo employeeToUpdate = existingEmployeeOpt.get();

        // Update fields from the incoming employee object
        employeeToUpdate.setName(employeeDetails.getName());
        employeeToUpdate.setPosition(employeeDetails.getPosition());
        employeeToUpdate.setSalary(employeeDetails.getSalary());
        employeeToUpdate.setStart_date(employeeDetails.getStart_date());
        employeeToUpdate.setOffice(employeeDetails.getOffice());
        employeeToUpdate.setExtn(employeeDetails.getExtn());
        employeeToUpdate.setHasManagerRights(employeeDetails.isHasManagerRights());

        // Only update the manager if a managerId is provided in the request.
        if (managerId != null) {
            if (managerId.isBlank() || "null".equalsIgnoreCase(managerId)) {
                employeeToUpdate.setManagerId(null);
            } else {
                if (id.equals(managerId)) {
                    throw new IllegalArgumentException("An employee cannot be their own manager.");
                }
                EmployeeMongo newManager = employeeMongoRepository.findById(managerId)
                        .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));
                detectCycle(employeeToUpdate, newManager);
                employeeToUpdate.setManagerId(managerId);
            }
        }

        return employeeMongoRepository.save(employeeToUpdate);
    }

    public boolean deleteEmployee(String id) {
        if (!employeeMongoRepository.existsById(id)) {
            return false;
        }
        employeeMongoRepository.deleteById(id);
        return true;
    }

    public List<EmployeeMongo> getAllEmployeesWithManagers() {
        List<EmployeeMongo> allEmployees = employeeMongoRepository.findAll();
        Map<String, EmployeeMongo> id2EmployeeMap = new HashMap<>();
        allEmployees.forEach(empl -> id2EmployeeMap.put(empl.getId(), empl));
        allEmployees.forEach(empl -> {
            if (empl.getManagerId() != null) {
                empl.setManager(id2EmployeeMap.get(empl.getManagerId()));
            }
        });
        return allEmployees;
    }
}
