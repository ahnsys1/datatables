package com.angular.backend.employees;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "employees")
public class EmployeeJPA {

    public EmployeeJPA(String id, String name, String position,
            String extn, String salary, LocalDate start_date, String office, EmployeeJPA manager, boolean hasManagerRights) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.extn = extn;
        this.salary = salary;
        this.start_date = start_date;
        this.office = office;
        this.manager = manager;
        this.hasManagerRights = hasManagerRights;
    }

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    public String name;
    public String position;
    public String extn;
    public String salary;
    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate start_date;
    public String office;
    @JsonDeserialize(using = StringToBooleanDeserializer.class)
    private boolean hasManagerRights = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private EmployeeJPA manager;

    @Transient
    public List<EmployeeJPA> children = new ArrayList<>();

    public EmployeeJPA() {

    }

    /**
     * A custom Jackson deserializer to convert String values ("1", "true", "0",
     * "false") into a boolean. This makes the API more robust to different ways
     * clients might represent boolean values in JSON.
     */
    public static class StringToBooleanDeserializer extends JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText();
            if ("true".equalsIgnoreCase(value)
                    || "1".equals(value)
                    || "yes".equalsIgnoreCase(value)) {
                return Boolean.TRUE;
            }
            // Treat "false", "0", null, or any other value as false.
            return Boolean.FALSE;
        }
    }

    public List<EmployeeJPA> getChildren() {
        return children;
    }

    /**
     * Adds a child to this employee's transient children list for building the
     * tree structure.
     */
    public void addChild(EmployeeJPA child) {
        // The `hasManagerRights` check was removed from here to ensure the tree is always
        // built correctly based on the actual manager relationships from the database.
        this.children.add(child);
    }

    public void removeChild(EmployeeJPA child) {
        if (child == null) {
            return;
        }
        // The relationship is defined by the child's manager.
        // Check if this employee is the manager of the child to be removed.
        if (this.equals(child.getManager())) {
            // Sever the relationship from the child's side (this is the persisted part).
            child.setManager(null);
            // Also remove the child from the transient list for in-memory consistency.
            this.children.remove(child);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EmployeeJPA that = (EmployeeJPA) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
