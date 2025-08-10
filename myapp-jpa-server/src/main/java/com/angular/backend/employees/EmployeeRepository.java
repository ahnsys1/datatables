package com.angular.backend.employees;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeJPA, String> {

    @Query("SELECT e FROM EmployeeJPA e LEFT JOIN FETCH e.manager")
    List<EmployeeJPA> findAllWithManagers();

    @Query("SELECT e FROM EmployeeJPA e LEFT JOIN FETCH e.manager WHERE e.id = :id")
    EmployeeJPA findByIdWithManager(@Param("id") String id);

    /**
     * Finds all employees who can be assigned as a manager. This includes root
     * employees (those without a manager) and any employee who has the
     * 'hasManagerRights' flag set to true.
     */
    @Query("SELECT e FROM EmployeeJPA e WHERE e.manager IS NULL OR e.hasManagerRights = true")
    List<EmployeeJPA> findPotentialManagers();

}
