package com.angular.backend.employees;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EmployeeCsvExportServiceTest {

    @TempDir
    Path exportDirectory;

    @Test
    void readIntradayChangesClearsExportedEvents() throws IOException {
        EmployeeCsvExportService service = new EmployeeCsvExportService(mock(EmployeeRepository.class),
                exportDirectory.toString(), "intra_day_changes.csv", "complete_employees.csv", "Europe/Prague");
        EmployeeJPA oldEmployee = employee("old-employee");
        EmployeeJPA newEmployee = employee("new-employee");

        service.recordChange("CREATE", null, oldEmployee);

        String exportedChanges = service.readIntradayChanges();

        assertTrue(exportedChanges.contains("old-employee"));
        assertFalse(Files.exists(exportDirectory.resolve("intra_day_changes.csv")));

        service.recordChange("CREATE", null, newEmployee);

        String newerChanges = service.readIntradayChanges();

        assertTrue(newerChanges.contains("new-employee"));
        assertFalse(newerChanges.contains("old-employee"));
    }

    private EmployeeJPA employee(String id) {
        EmployeeJPA employee = new EmployeeJPA();
        employee.setId(id);
        employee.setName("Employee " + id);
        return employee;
    }
}