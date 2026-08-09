package com.angular.backend.employees;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmployeeCsvExportService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeCsvExportService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String CHANGE_TITLE = "Intra-day employee changes";
    private static final String[] CHANGE_HEADER = {
            "timestamp", "action", "employee_id", "old_name", "new_name", "old_position", "new_position",
            "old_extn", "new_extn", "old_salary", "new_salary", "old_start_date", "new_start_date",
            "old_office", "new_office", "old_has_manager_rights", "new_has_manager_rights", "old_manager_id",
            "new_manager_id", "old_manager_name", "new_manager_name"
    };
    private static final String[] EMPLOYEE_HEADER = {
            "employee_id", "name", "position", "extn", "salary", "start_date", "office", "has_manager_rights",
            "manager_id", "manager_name"
    };

    private final EmployeeRepository employeeRepository;
    private final Path exportDirectory;
    private final String changesFileName;
    private final String completeEmployeesFileName;
    private final ZoneId zoneId;

    public EmployeeCsvExportService(
            EmployeeRepository employeeRepository,
            @Value("${app.employee-export.directory:${user.home}}") String exportDirectory,
            @Value("${app.employee-export.changes-file:intra_day_changes.csv}") String changesFileName,
            @Value("${app.employee-export.complete-file:complete_employees.csv}") String completeEmployeesFileName,
            @Value("${app.employee-export.time-zone:Europe/Prague}") String timeZone) {
        this.employeeRepository = employeeRepository;
        this.exportDirectory = Path.of(exportDirectory);
        this.changesFileName = changesFileName;
        this.completeEmployeesFileName = completeEmployeesFileName;
        this.zoneId = ZoneId.of(timeZone);
    }

    public synchronized void recordChange(String action, EmployeeJPA oldEmployee, EmployeeJPA newEmployee) {
        Path file = exportDirectory.resolve(changesFileName);
        String timestamp = LocalDateTime.now(zoneId).format(TIMESTAMP_FORMAT);
        StringBuilder row = new StringBuilder(timestamp).append(',').append(csv(action));
        appendEmployeePair(row, oldEmployee, newEmployee);
        appendChangeFileHeaderIfNeeded(file);
        appendLine(file, row.toString());
    }

    @Scheduled(cron = "${app.employee-export.cron:0 59 23 * * *}", zone = "${app.employee-export.time-zone:Europe/Prague}")
    public synchronized void exportCompleteEmployees() {
        Path file = exportDirectory.resolve(completeEmployeesFileName);
        StringBuilder content = new StringBuilder();
        content.append(joinCsv(List.of(EMPLOYEE_HEADER))).append('\n');
        for (EmployeeJPA employee : employeeRepository.findAllWithManagers()) {
            content.append(employeeRow(employee)).append('\n');
        }
        try {
            Files.createDirectories(exportDirectory);
            Files.writeString(file, content.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            log.info("Exported {} employees to {}", employeeRepository.count(), file);
        } catch (IOException exception) {
            log.error("Could not write complete employee export to {}", file, exception);
        }
    }

    private void appendChangeFileHeaderIfNeeded(Path file) {
        try {
            Files.createDirectories(exportDirectory);
            if (!Files.exists(file) || Files.size(file) == 0) {
                Files.writeString(file, CHANGE_TITLE + '\n' + joinCsv(List.of(CHANGE_HEADER)) + '\n', StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize employee changes file " + file, exception);
        }
    }

    private void appendLine(Path file, String line) {
        try {
            Files.writeString(file, line + '\n', StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write employee change to " + file, exception);
        }
    }

    private void appendEmployeePair(StringBuilder row, EmployeeJPA oldEmployee, EmployeeJPA newEmployee) {
        appendEmployeeFields(row, oldEmployee);
        appendEmployeeFields(row, newEmployee);
    }

    private void appendEmployeeFields(StringBuilder row, EmployeeJPA employee) {
        if (employee == null) {
            for (int index = 0; index < 10; index++) {
                row.append(',');
            }
            return;
        }
        row.append(',').append(csv(employee.getId())).append(',').append(csv(employee.getName()))
                .append(',').append(csv(employee.getPosition())).append(',').append(csv(employee.getExtn()))
                .append(',').append(csv(employee.getSalary())).append(',').append(csv(employee.getStart_date()))
                .append(',').append(csv(employee.getOffice())).append(',').append(employee.isHasManagerRights())
                .append(',').append(csv(employee.getManager() == null ? null : employee.getManager().getId()))
                .append(',').append(csv(employee.getManager() == null ? null : employee.getManager().getName()));
    }

    private String employeeRow(EmployeeJPA employee) {
        String[] values = {
                employee.getId(), employee.getName(), employee.getPosition(), employee.getExtn(), employee.getSalary(),
                String.valueOf(employee.getStart_date()), employee.getOffice(), String.valueOf(employee.isHasManagerRights()),
                employee.getManager() == null ? null : employee.getManager().getId(),
                employee.getManager() == null ? null : employee.getManager().getName()
        };
        return joinCsv(List.of(values));
    }

    private String joinCsv(List<String> values) {
        return values.stream().map(this::csv).reduce((left, right) -> left + ',' + right).orElse("");
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}