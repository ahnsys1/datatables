package com.angular.backend.employees;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeMongoRepository extends MongoRepository<EmployeeMongo, String> {

    /**
     * Finds all employees who can be assigned as a manager. This includes root
     * employees (those without a manager) and any employee who has the
     * 'hasManagerRights' flag set to true.
     */
    @Query("{'$or': [{'manager_id': null}, {'has_manager_rights': true}]}")
    List<EmployeeMongo> findPotentialManagers();
}
