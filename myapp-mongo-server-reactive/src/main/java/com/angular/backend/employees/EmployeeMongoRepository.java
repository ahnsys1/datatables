package com.angular.backend.employees;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeMongoRepository extends ReactiveMongoRepository<EmployeeMongo, String> {
}