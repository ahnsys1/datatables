package com.angular.backend.employees;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeMongoService {

    private final EmployeeMongoRepository employeeMongoRepository;

    // Using constructor injection is a best practice for required dependencies.
    public EmployeeMongoService(EmployeeMongoRepository employeeMongoRepository) {
        this.employeeMongoRepository = employeeMongoRepository;
    }

    public Flux<EmployeeMongo> getAllEmployees() {
        return this.employeeMongoRepository.findAll();
    }

    public Mono<EmployeeMongo> getEmployeeById(String id) {
        return this.employeeMongoRepository.findById(id);
    }

    public Mono<EmployeeMongo> createEmployee(EmployeeMongo employee) {
        if (employee.getId() == null || employee.getId().equals("")) {
            employee.setId(null); // Ensure the ID is null for new records
        }
        return this.employeeMongoRepository.save(employee);
    }

    public Mono<EmployeeMongo> updateEmployee(String id, EmployeeMongo employee) {
        return this.employeeMongoRepository.findById(id)
                .flatMap(existingEmployee -> {
                    existingEmployee.setName(employee.getName());
                    existingEmployee.setPosition(employee.getPosition());
                    existingEmployee.setSalary(employee.getSalary());
                    existingEmployee.setOffice(employee.getOffice());
                    existingEmployee.setExtn(employee.getExtn());
                    existingEmployee.setStart_date(employee.getStart_date());
                    return this.employeeMongoRepository.save(existingEmployee);
                });
    }

    public Mono<Void> deleteEmployee(String id) {
        return this.employeeMongoRepository.deleteById(id);
    }
}
