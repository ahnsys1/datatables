package com.angular.backend.employees;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "employees")
public class EmployeeMongo {

    public EmployeeMongo(String name, String position,
            String extn, String salary, String start_date, String office) {
        this.name = name;
        this.position = position;
        this.extn = extn;
        this.salary = salary;
        this.start_date = start_date;
        this.office = office;
    }

    @Id
    private String id;
    private String name;
    private String position;
    private String extn;
    private String salary;
    private String start_date;
    private String office;

    public EmployeeMongo() {
    }
}
