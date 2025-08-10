package com.angular.backend.employees;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/employees")
public class EmployeeMongoController {

    private final EmployeeMongoService employeeMongoService;

    public EmployeeMongoController(EmployeeMongoService employeeMongoService) {
        this.employeeMongoService = employeeMongoService;
    }

    @GetMapping
    public Flux<EmployeeMongo> getAllEmployees() {
        return employeeMongoService.getAllEmployees();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EmployeeMongo> createEmployee(@RequestBody EmployeeMongo employee) {
        return employeeMongoService.createEmployee(employee);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<EmployeeMongo>> getEmployeeById(@PathVariable String id) {
        return employeeMongoService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<EmployeeMongo>> updateEmployee(@PathVariable String id, @RequestBody EmployeeMongo employeeDetails) {
        return employeeMongoService.updateEmployee(id, employeeDetails)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteEmployee(@PathVariable String id) {
        // This assumes employeeMongoService.deleteEmployee returns an empty Mono if not found.
        return employeeMongoService.deleteEmployee(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
