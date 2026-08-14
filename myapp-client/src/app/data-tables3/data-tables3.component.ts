import { Component, OnInit, WritableSignal, signal } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Config } from 'datatables.net';
import 'datatables.net-buttons-dt';
import DataTable from 'datatables.net-dt';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AddEmployeeComponent } from '../add-employee/add-employee.component';
import { EmployeeService } from '../service/EmployeeService';
import { Employee } from '../shared/model/Employee';
import { DatePipe } from '@angular/common';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../shared/confirmation-dialog/confirmation-dialog.component';
import { ErrorDialogComponent, ErrorDialogData } from '../shared/error-dialog/error-dialog.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TranslatePipe } from '@ngx-translate/core';
import { forkJoin, from, of, throwError } from 'rxjs';
import { catchError, concatMap, toArray } from 'rxjs/operators';
import { SpinnerComponent } from '../shared/spinner/spinner.component';
import { SpinnerService } from '../service/spinner.service';

@Component({
  selector: 'app-data-tables3',
  templateUrl: './data-tables3.component.html',
  imports: [MatProgressSpinnerModule, SpinnerComponent],
  styleUrl: './data-tables3.component.scss'
})
export class DataTables3Component implements OnInit {

  private normalizeIntradayFields(fields: string[]): string[] {
    if (fields[1] === 'UPDATE' && fields.length === 22) {
      const normalized = fields.slice(0, 3);
      for (let index = 0; index < 9; index++) {
        normalized.push(fields[3 + index], fields[13 + index]);
      }
      return normalized;
    }
    if (fields[1] === 'CREATE' && fields.length === 21 && !fields[2]) {
      const employeeIdIndex = fields.findIndex((value, index) => index >= 3 && value.length > 0);
      if (employeeIdIndex > 2) {
        const normalized = fields.slice(0, 2);
        normalized.push(fields[employeeIdIndex]);
        for (let index = 0; index < 9; index++) {
          normalized.push('', fields[employeeIdIndex + 1 + index] ?? '');
        }
        return normalized;
      }
    }
    return fields;
  }

  private table!: any;
  private employeeIdToEmployeeMap: Map<string, Employee> = new Map();
  public dtOptions: Config = {};
  // Create a writable signal for the loading state, initialized to false.
  //  loading: WritableSignal<boolean> = signal(false);
  isLoading: WritableSignal<boolean> = signal(false);
  constructor(private dialog: MatDialog, private employeeService: EmployeeService,
    private translate: TranslateService, private spinnerService: SpinnerService) { }
  ngOnInit(): void {
    this.spinnerService.show();
    // Using `get` ensures that the translations are loaded before we try to use them.
    // `instant` can fail if the component initializes before the translation file is fetched.
    this.translate.get([
      'datatables.info', 'new-employee', 'edit-employee', 'remove-employee', 'confirm-delete',
      'confirm-delete-message', 'delete', 'cancel', 'id', 'name', 'manager', 'position',
      'office', 'extn', 'start-date', 'salary', 'is-manager', 'yes', 'no'
    ]).subscribe(translations => {
      this.dtOptions = {
        language: {
          info: translations['datatables.info']
        },
        layout: {
          topEnd: {
            buttons: [
              {
                text: 'Import employees',
                className: 'btn btn-outline-primary',
                action: () => this.importEmployees()
              },
              {
                text: 'Export employees',
                className: 'btn btn-outline-secondary',
                action: () => this.exportEmployees()
              },
              {
                text: 'Import changes',
                className: 'btn btn-outline-primary',
                action: () => this.importIntradayChanges()
              },
              {
                text: 'Export changes',
                className: 'btn btn-outline-secondary',
                action: () => this.exportIntradayChanges()
              },
              {
                text: 'Delete employees',
                className: 'btn btn-outline-danger',
                action: () => this.deleteAllEmployees()
              }
            ]
          },
          topStart: {
            search: {
              placeholder: 'Search employees'
            },
            buttons: [
              {
                text: translations['new-employee'],
                className: "btn btn-primary",
                action: () => {
                  const dialogRef: MatDialogRef<AddEmployeeComponent> = this.dialog.open(AddEmployeeComponent,
                    {
                      disableClose: true,
                      data: null
                    });
                  dialogRef.afterClosed().subscribe(result => {
                    if (!result) {
                      return;
                    }
                    const e: Employee = new Employee(
                      "", // id is generated by backend
                      result.name,
                      result.position,
                      result.salary,
                      result.start_date,
                      result.office,
                      result.extn,
                      result.hasManagerRights,
                      result.manager);
                    this.addEmployee(e);
                  });
                }

              },
              {
                text: translations['edit-employee'],
                className: "my-button my-disabled-button btn btn-primary",
                name: "editButton",
                action: () => {
                  const rowData = this.table.row(".selected").data();
                  if (rowData == null || rowData == undefined) {
                    return;
                  }
                  const dialogRef: MatDialogRef<AddEmployeeComponent> =
                    this.dialog.open(AddEmployeeComponent,
                      {
                        disableClose: true,
                        data: rowData
                      }
                    )

                  dialogRef.afterClosed().subscribe(result => {
                    if (!result) {
                      return;
                    }
                    const e: Employee = new Employee(
                      result.id,
                      result.name,
                      result.position,
                      result.salary,
                      result.start_date,
                      result.office,
                      result.extn,
                      result.hasManagerRights,
                      result.manager);
                    this.editEmployee(e);
                  });

                }
              },
              {
                text: translations['remove-employee'],
                className: "my-button my-disabled-button btn btn-primary",
                name: "deleteButton",
                action: () => {
                  const rowId = this.table.row('.selected').id();
                  if (rowId != undefined && rowId != null) {
                    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
                      data: {
                        title: translations['confirm-delete'],
                        message: translations['confirm-delete-message'],
                        confirmText: translations['delete'],
                        cancelText: translations['cancel']
                      } as ConfirmationDialogData
                    });
                    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
                      if (confirmed) {
                        this.deleteEmployee(rowId);
                      }
                    });
                  }
                }
              }
            ]
          }
        },
        columns: [
          { data: 'id', title: translations['id'], visible: false },
          { data: 'name', title: translations['name'] },
          {
            data: 'manager',
            title: translations['manager'],
            defaultContent: "", // This prevents the error if 'manager' property is missing.
            render: (data, type, row) => {
              // For display and filtering, we want to show the manager's name.
              if (type === 'display' || type === 'filter') {
                // If `data` (which is `row.manager`) is null, there's no manager.
                if (data == null) {
                  return "";
                }

                // If `data` is a string, it's a manager's ID. Look it up in the map.
                if (typeof data === 'string') {
                  return this.employeeIdToEmployeeMap.get(data)?.name || '';
                }

                // If `data` is an object, it should have a `name` property.
                // This handles both Employee instances and plain objects from JSON.
                if (typeof data === 'object' && data.name) {
                  return data.name;
                }
              }

              // For sorting or other types, return the raw data.
              return data;
            }
          },
          { data: 'position', title: translations['position'] },
          { data: 'office', title: translations['office'] },
          { data: 'extn', title: translations['extn'] },
          { data: 'start_date', title: translations['start-date'], render: (data: any) => new DatePipe('cs-CZ').transform(data, 'dd.MM.yyyy') ?? '' },
          { data: 'salary', title: translations['salary'] },
          {
            data: 'hasManagerRights', title: translations['is-manager'], render: (data) => {
              // An employee is a manager *only* if `hasManagerRights` is true.
              // This corrects the logic that also considered top-level employees as managers.
              return data ? translations['yes'] : translations['no'];
            }
          },
        ],
        pageLength: 100, // The rest of your config is fine
        lengthMenu: [5, 10, 20, 50, 100], // ...
        deferRender: true,
        autoWidth: false,
        lengthChange: true, // ...
        info: true, // ...
        infoCallback(settings, start, end, max, total, pre) { // ...
          return 'From ' + start + ' to ' + end + " of " + max + ' rows<br/>'; // ...
        }, // ...
        rowId: "id" // ...
      };
      this.table = new DataTable($('#jsonTable3'), this.dtOptions);
      this.getEmployees();

      const tableBody = $('#jsonTable3 tbody');
      tableBody.on('click', 'tr', event => {
        tableBody.find('tr.selected').removeClass('selected');
        $(event.currentTarget).addClass('selected');
        $('.my-button').removeClass('my-disabled-button');
      });
      this.table.on('page order search', () => {
        tableBody.find('tr.selected').removeClass('selected');
        $('.my-button').addClass('my-disabled-button');
      });
    });
    
  };


  addEmployee(emp: Employee): void {



    this.employeeService.createEmployee(emp).subscribe({
      next: (res: Employee) => {
        this.employeeIdToEmployeeMap.set(res.id, res);
        this.table.row.add(res).draw();
      },
      error: (err: any) => {
        alert(this.translate.instant('failed-to-add-employee') + ': ' + err.message);
      }
    });
  }

  editEmployee(emp: Employee): void {
    this.employeeService.updateEmployee(emp).subscribe({
      next: (res: Employee) => {
        const rowToUpdate = this.table.row('.selected');
        const oldData = rowToUpdate.data() || {}; // Safely get old data
        const newData = { ...oldData, ...res };

        // Update the map to prevent stale manager data for other rows
        this.employeeIdToEmployeeMap.set(newData.id, newData);

        rowToUpdate.data(newData);

        // Invalidate all rows to re-run render functions (e.g., for manager names) and redraw.
        this.table.rows().invalidate().draw(false);
      },
      error: (err: any) => {
        const dialogRef = this.dialog.open(ErrorDialogComponent, {
          data: {
            title: this.translate.instant('cycle-detected'),
            message: this.translate.instant('user-cannot-be-own-manager'),
            confirmText: this.translate.instant('ok'),

          } as ErrorDialogData
        });
      }
    });
  }


  deleteEmployee(empId: string) {
    this.employeeService.deleteEmployee(empId).subscribe({
      next: () => {
        this.employeeIdToEmployeeMap.delete(empId);
        this.table.row(".selected").remove();
        // Invalidate all rows to re-run render functions (e.g., for manager names)
        // and redraw the table without changing pagination.
        this.table.rows().invalidate().draw(false);
      },
      error: (err: any) => {
        this.translate.get(['manager-cannot-be-deleted', 'employee-cannot-be-deleted-has-subordinates'])
          .subscribe(translations => {
            this.dialog.open(ErrorDialogComponent, {
              data: {
                title: translations['manager-cannot-be-deleted'],
                message: translations['employee-cannot-be-deleted-has-subordinates'],
                confirmText: 'OK',
              } as ErrorDialogData
            });
          });
      }
    });
  }

  private exportEmployees(): void {
    this.spinnerService.show();
    setTimeout(() => {
      try {
        const employees = this.sortEmployeesParentFirst(this.table.rows().data().toArray() as Employee[]);
        const blob = new Blob([JSON.stringify(employees, null, 2)], { type: 'application/json' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'employees.json';
        link.click();
        URL.revokeObjectURL(link.href);
      } finally {
        this.spinnerService.hide();
      }
    });
  }

  private importEmployees(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json,application/json';
    input.onchange = () => {
      const file = input.files?.[0];
      if (!file) {
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        this.spinnerService.show();
        try {
          const employees = JSON.parse(String(reader.result));
          if (!Array.isArray(employees)) {
            throw new Error('The file must contain an employee array.');
          }
          const restorePayload = this.sortEmployeesParentFirst(employees).map(employee => ({
            id: employee.id,
            name: employee.name,
            position: employee.position,
            extn: employee.extn,
            salary: employee.salary,
            start_date: employee.start_date,
            office: employee.office,
            hasManagerRights: employee.hasManagerRights,
            managerId: this.getManagerId(employee)
          }));
          this.employeeService.restoreEmployees(restorePayload).subscribe({
            next: () => this.getEmployees(),
            error: error => {
              this.spinnerService.hide();
              alert(this.translate.instant('failed-to-add-employee') + ': ' + error.message);
            }
          });
        } catch (error: any) {
          this.spinnerService.hide();
          alert('Could not import employees: ' + error.message);
        }
      };
      reader.onerror = () => {
        this.spinnerService.hide();
        alert('Could not read the employee file.');
      };
      reader.readAsText(file);
    };
    input.click();
  }

  private exportIntradayChanges(): void {
    this.spinnerService.show();
    this.employeeService.getIntradayChanges().subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'intra_day_changes.csv';
        document.body.appendChild(link);
        link.click();
        link.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
        this.spinnerService.hide();
      },
      error: error => {
        this.spinnerService.hide();
        alert('Could not export intraday changes: ' + error.message);
      }
    });
  }

  private importIntradayChanges(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,text/csv';
    input.onchange = () => {
      const file = input.files?.[0];
      if (!file) {
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        this.spinnerService.show();
        try {
          console.debug('[employee-import] Reading intraday changes', {
            fileName: file.name,
            fileSize: file.size
          });
          const changes = this.parseIntradayChanges(String(reader.result));
          console.debug('[employee-import] Parsed intraday changes', {
            rowCount: changes.length,
            actions: changes.map(change => change.action)
          });
          const operations = changes.filter(change => ['CREATE', 'UPDATE', 'DELETE'].includes(change.action));
          console.debug('[employee-import] Prepared import operations', {
            operations: operations.map(change => ({ action: change.action, employeeId: change.employeeId }))
          });
          from(operations).pipe(
            concatMap(change => {
              const employee = {
                id: change.employeeId, name: change.newName, position: change.newPosition,
                extn: change.newExtn, salary: change.newSalary, start_date: change.newStartDate,
                office: change.newOffice,
                hasManagerRights: change.newHasManagerRights.toLowerCase() === 'true',
                managerId: change.newManagerId || null,
                manager: change.newManagerId ? ({ id: change.newManagerId } as Employee) : null
              } as Employee;
              if (change.action === 'CREATE') {
                console.debug('[employee-import] Replaying CREATE in CSV order', { employeeId: change.employeeId });
                return this.employeeService.restoreEmployees([employee], false);
              }
              if (change.action === 'UPDATE') {
                console.debug('[employee-import] Replaying UPDATE', { employeeId: change.employeeId });
                return this.employeeService.updateEmployee(employee, false);
              }
              console.debug('[employee-import] Replaying DELETE', { employeeId: change.employeeId });
              return this.employeeService.deleteEmployee(change.employeeId, false).pipe(
                catchError(error => {
                  if (error.status === 404) {
                    console.warn('[employee-import] DELETE skipped; employee is already absent', {
                      employeeId: change.employeeId
                    });
                    return of(null);
                  }
                  return throwError(() => error);
                })
              );
            }),
            toArray()
          ).subscribe({
            next: () => {
              console.debug('[employee-import] Change replay completed', { count: operations.length });
              this.getEmployees();
            },
            error: error => {
              console.error('[employee-import] Delete request failed', error);
              this.spinnerService.hide();
              alert('Could not import intraday changes: ' + error.message);
            }
          });
        } catch (error: any) {
          console.error('[employee-import] Failed to parse or prepare changes', error);
          this.spinnerService.hide();
          alert('Could not import intraday changes: ' + error.message);
        }
      };
      reader.onerror = () => {
        console.error('[employee-import] Could not read changes file', {
          fileName: file.name,
          error: reader.error
        });
        this.spinnerService.hide();
        alert('Could not read the changes file.');
      };
      reader.readAsText(file);
    };
    input.click();
  }

  private parseIntradayChanges(content: string): any[] {
    const lines = content.split(/\r?\n/).filter(line => line.trim().length > 0);
    const headerLine = lines[0]?.replace(/^\uFEFF/, '').trim() ?? '';
    const headerFields = this.parseCsvLine(headerLine);
    console.debug('[employee-import] Intraday changes header', {
      rawHeader: lines[0] ?? '',
      parsedHeader: headerFields
    });
    if (headerFields[0] !== 'timestamp' || headerFields[1] !== 'action') {
      throw new Error('The file does not contain a valid intraday changes header.');
    }
    return lines.slice(1).map(line => {
      const fields = this.normalizeIntradayFields(this.parseCsvLine(line));
      if (fields.length !== 21) {
        throw new Error('A changes row must contain 21 columns.');
      }
      return {
        action: fields[1], employeeId: fields[2], newName: fields[4], newPosition: fields[6],
        newExtn: fields[8], newSalary: fields[10], newStartDate: fields[12], newOffice: fields[14],
        newHasManagerRights: fields[16], newManagerId: fields[18], oldManagerId: fields[17]
      };
    });
  }

  private parseCsvLine(line: string): string[] {
    const fields: string[] = [];
    let field = '';
    let quoted = false;
    for (let index = 0; index < line.length; index++) {
      const character = line[index];
      if (character === '"' && line[index + 1] === '"' && quoted) {
        field += '"'; index++;
      } else if (character === '"') {
        quoted = !quoted;
      } else if (character === ',' && !quoted) {
        fields.push(field); field = '';
      } else { field += character; }
    }
    fields.push(field);
    return fields;
  }

  private sortChangesParentFirst(changes: any[]): any[] {
    const byId = new Map(changes.map(change => [change.id, change]));
    const ordered: any[] = [];
    const visited = new Set<string>();
    const stack = changes.filter(change => !change.managerId || !byId.has(change.managerId)).reverse();
    while (stack.length > 0) {
      const change = stack.pop();
      if (!change || visited.has(change.id)) continue;
      visited.add(change.id);
      ordered.push(change);
      stack.push(...changes.filter(candidate => candidate.managerId === change.id).reverse());
    }
    return [...ordered, ...changes.filter(change => !visited.has(change.id))];
  }

  private sortChangesChildrenFirst(changes: any[]): any[] {
    const byId = new Map(changes.map(change => [change.employeeId, change]));
    const depth = (change: any, path = new Set<string>()): number => {
      if (!change.oldManagerId || !byId.has(change.oldManagerId) || path.has(change.employeeId)) return 0;
      return depth(byId.get(change.oldManagerId), new Set(path).add(change.employeeId)) + 1;
    };
    return [...changes].sort((left, right) => depth(right) - depth(left));
  }

  private getManagerId(employee: Employee): string | null {
    return employee.manager?.id ?? employee.managerId ?? null;
  }

  private sortEmployeesParentFirst(employees: Employee[]): Employee[] {
    const byId = new Map(employees.map(employee => [employee.id, employee]));
    const ordered: Employee[] = [];
    const visited = new Set<string>();
    const stack = employees.filter(employee => {
      const managerId = this.getManagerId(employee);
      return !managerId || !byId.has(managerId);
    }).reverse();
    while (stack.length > 0) {
      const employee = stack.pop();
      if (!employee || visited.has(employee.id)) continue;
      visited.add(employee.id);
      ordered.push(employee);
      stack.push(...employees.filter(candidate => this.getManagerId(candidate) === employee.id).reverse());
    }
    return [...ordered, ...employees.filter(employee => !visited.has(employee.id))];
  }

  private deleteAllEmployees(): void {
    const employees = this.table.rows().data().toArray() as Employee[];
    if (employees.length === 0) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: this.translate.instant('confirm-delete'),
        message: this.translate.instant('confirm-delete-message'),
        confirmText: this.translate.instant('delete'),
        cancelText: this.translate.instant('cancel')
      } as ConfirmationDialogData
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteEmployeeLayers(employees);
      }
    });
  }

  private deleteEmployeeLayers(employees: Employee[]): void {
    this.employeeService.deleteAllEmployees().subscribe({
      next: () => this.getEmployees(),
      error: error => alert('Could not delete employees: ' + error.message)
    });
  }

  getEmployees(): void {
    this.isLoading.set(true);
    this.employeeIdToEmployeeMap.clear();
    this.table.clear();
    this.employeeService.getEmployeesWithManagers().subscribe({
      next: (res: any[]) => {
        if (!res || res.length === 0) {
          this.table.draw();
          this.isLoading.set(false);
          this.spinnerService.hide();
          return;
        }

        const directEmployees = res.filter(
          (employee): employee is Employee => typeof employee === 'object' && employee !== null && 'id' in employee
        );
        const employeeIds = res.filter((employee): employee is string => typeof employee === 'string');

        const finishLoading = (employees: Employee[]) => {
          employees.forEach(employee => this.employeeIdToEmployeeMap.set(employee.id, employee));
          if (employees.length > 0) {
            this.table.rows.add(employees).draw(false);
          } else {
            this.table.draw(false);
          }
          this.isLoading.set(false);
          this.spinnerService.hide();
        };

        if (employeeIds.length === 0) {
          finishLoading(directEmployees);
          return;
        }

        forkJoin(employeeIds.map(id => this.employeeService.getEmployeeWithManager(id))).subscribe({
          next: loadedEmployees => finishLoading([...directEmployees, ...loadedEmployees]),
          error: (err: any) => {
            alert(this.translate.instant('failed-to-get-employees') + ': ' + err.message);
            this.isLoading.set(false);
            this.spinnerService.hide();
          }
        });

      },
      error: (err: any) => {
        alert(this.translate.instant('failed-to-get-employees') + ': ' + err.message);
        this.isLoading.set(false);
        this.spinnerService.hide();
      }
    });
  }

}
