package com.angular.backend.employees;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "employees")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EmployeeMongo {

    public EmployeeMongo(String id, String name, String position,
            String extn, String salary, LocalDate start_date, String office, String managerId, boolean hasManagerRights) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.extn = extn;
        this.salary = salary;
        this.start_date = start_date;
        this.office = office;
        this.managerId = managerId;
        this.hasManagerRights = hasManagerRights;
    }

    @Id
    private String id;
    private String name;
    private String position;
    private String extn;
    private String salary;

    @Field("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate start_date;
    private String office;

    @Field("has_manager_rights")
    private boolean hasManagerRights = false;

    @Field("manager_id")
    private String managerId;

    @Transient
    @JsonIgnore
    private EmployeeMongo manager;

    @Transient
    private List<EmployeeMongo> children = new ArrayList<>();

    public EmployeeMongo() {
    }

    public void addChild(EmployeeMongo child) {
        this.children.add(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmployeeMongo that = (EmployeeMongo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
