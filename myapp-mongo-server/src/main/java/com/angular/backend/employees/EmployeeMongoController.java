package com.angular.backend.employees;

import java.util.List;
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
public class EmployeeMongoController {

    @Autowired
    private EmployeeMongoService employeeMongoService;

    @PostMapping
    public ResponseEntity<EmployeeMongo> createEmployee(@RequestBody EmployeeMongo employee, @RequestParam(required = false) String managerId) {
        employee.setId(null);
        EmployeeMongo createdEmployee;
        if (managerId != null) {
            createdEmployee = employeeMongoService.createEmployee(employee, managerId);
        } else {
            createdEmployee = employeeMongoService.createEmployee(employee);
        }
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Iterable<EmployeeMongo>> getAllEmployees() {
        return new ResponseEntity<>(employeeMongoService.getAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/with-managers")
    public ResponseEntity<Iterable<EmployeeMongo>> getAllEmployeesWirhManagers() {
        List<EmployeeMongo> allEmployeesWithManagers = employeeMongoService.getAllEmployeesWithManagers();
        return new ResponseEntity<>(allEmployeesWithManagers, HttpStatus.OK);
    }

    @GetMapping("/root")
    public ResponseEntity<Iterable<EmployeeMongo>> getEmployeeTree() {
        List<EmployeeMongo> rootEmployees = employeeMongoService.getEmployeeTree();
        return new ResponseEntity<>(rootEmployees, HttpStatus.OK);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<EmployeeMongo>> getAllManagers() {
        List<EmployeeMongo> managers = employeeMongoService.getPotentialManagers();
        return new ResponseEntity<>(managers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeMongo> getEmployeeById(@PathVariable String id) {
        EmployeeMongo employee = employeeMongoService.getEmployeeById(id);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeMongo> updateEmployee(@PathVariable String id, @RequestBody EmployeeMongo employee,
            @RequestParam(required = false) String managerId) {
        EmployeeMongo updatedEmployee = employeeMongoService.updateEmployee(id, employee, managerId);
        if (updatedEmployee == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        boolean deleted = employeeMongoService.deleteEmployee(id);
        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
