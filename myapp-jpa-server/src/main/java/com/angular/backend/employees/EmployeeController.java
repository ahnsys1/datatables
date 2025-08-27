package com.angular.backend.employees;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeJPA> createEmployee(@RequestBody EmployeeJPA employee, @RequestParam(required = false) String managerId) {
        EmployeeJPA newEmployee = employeeService.createEmployee(employee, managerId);
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Iterable<EmployeeJPA>> getAllEmployees() {
        List<EmployeeJPA> allEmployees = employeeService.getAllEmployees();
        return new ResponseEntity<>(allEmployees, HttpStatus.OK);
    }

    @GetMapping("/with-managers")
    public ResponseEntity<Iterable<EmployeeJPA>> getAllEmployeesWirhManagers() {
        List<EmployeeJPA> allEmployeesWithManagers = employeeService.getAllEmployeesWithManagers();
        return new ResponseEntity<>(allEmployeesWithManagers, HttpStatus.OK);
    }

    @GetMapping("/tree")
    public ResponseEntity<Iterable<EmployeeJPA>> getEmployeeTree() {
        List<EmployeeJPA> rootEmployees = employeeService.getEmployeeTree();
        return new ResponseEntity<>(rootEmployees, HttpStatus.OK);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<EmployeeJPA>> getAllManagers() {
        List<EmployeeJPA> managers = employeeService.getPotentialManagers();
        return new ResponseEntity<>(managers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeJPA> getEmployeeById(@PathVariable String id) {
        EmployeeJPA employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping("/employeeNameExists/{employeeName}")
    public ResponseEntity<Optional<Boolean>> isEmployeeNameExisting(@PathVariable String employeeName) {
        List<EmployeeJPA> allEmployees = employeeService.getAllEmployees();
        boolean exists = allEmployees.stream().anyMatch(emp -> emp.getName().equalsIgnoreCase(employeeName));
        return new ResponseEntity<>(Optional.of(exists), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeJPA> updateEmployee(@PathVariable String id, @RequestBody EmployeeJPA employee,
            @RequestParam(required = false) String managerId) {
        EmployeeJPA updatedEmployee = employeeService.updateEmployee(id, employee, managerId);
        if (updatedEmployee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        boolean deleted = employeeService.deleteEmployee(id);
        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
