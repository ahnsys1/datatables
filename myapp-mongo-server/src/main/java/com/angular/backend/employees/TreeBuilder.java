package com.angular.backend.employees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TreeBuilder {

    @Autowired
    private EmployeeMongoRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<EmployeeMongo> buildTree() {
        List<EmployeeMongo> employees = employeeRepository.findAll();

        Map<String, EmployeeMongo> employeeMap = new HashMap<>();
        for (EmployeeMongo emp : employees) {
            employeeMap.put(emp.getId(), emp);
        }

        List<EmployeeMongo> rootNodes = new ArrayList<>();
        for (EmployeeMongo employee : employees) {
            String managerId = employee.getManagerId();
            if (managerId != null) {
                EmployeeMongo manager = employeeMap.get(managerId);
                if (manager != null) {
                    manager.addChild(employee);
                }
            } else {
                rootNodes.add(employee);
            }
        }
        return rootNodes;
    }

}
