import { CommonModule } from '@angular/common';
import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EmployeeService } from '../service/EmployeeService';
import { Employee } from '../shared/model/Employee';
import { TranslateModule } from '@ngx-translate/core';
import { TranslatePipe } from '@ngx-translate/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { Observable, of, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';

declare var $: any; // jQuery

@Component({
  selector: 'app-add-employee',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule, TranslatePipe,
    MatAutocompleteModule],
  providers: [
  ],
  templateUrl: './add-employee.component.html',
  styleUrls: ['./add-employee.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddEmployeeComponent implements OnInit, AfterViewInit, OnDestroy {
  onClose() {
    this.dialogRef.close();
  }

  employeeObject: Employee = new Employee('', '', '', '', '', '', '', false, null);

  isEditMode: boolean = false;
  managers: Employee[] = [];
  employeeNameExists = false;
  filteredEmployees$!: Observable<Employee[]>;
  private nameChangeSubject = new Subject<string>();
  private originalName: string | undefined;

  constructor(
    public dialogRef: MatDialogRef<AddEmployeeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Employee | null,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef
  ) {
    this.isEditMode = !!data?.id;

    // Initialize employee object with data if in edit mode
    if (this.data) {
      this.employeeObject = { ...this.data };
      if (this.isEditMode) {
        this.originalName = this.data.name;
      }
    }


  }
  ngAfterViewInit(): void {
    // Initialize jQuery UI datepicker
    $('#startDate').datepicker({
      dateFormat: 'yy-mm-dd',
      onSelect: (dateText: string) => {
        this.employeeObject.start_date = dateText;
      }
    });

    // Set initial value if editing
    if (this.employeeObject.start_date) {
      $('#startDate').datepicker('setDate', this.employeeObject.start_date);
    }
  }

  ngOnDestroy(): void {
    // Destroy datepicker to avoid memory leaks
    $('#startDate').datepicker('destroy');
  }

  ngOnInit(): void {
    this.loadManagers();
    this.setupNameAutocomplete();
    console.log('AddEmployeeComponent initialized', {
      employeeObject: this.employeeObject,
    });
    this.filteredEmployees$ = this.employeeService.getEmployees();
  }

  private setupNameAutocomplete(): void {
    this.filteredEmployees$ = this.nameChangeSubject.pipe(
      debounceTime(300), // Wait for 300ms pause in typing
      distinctUntilChanged(), // Ignore if the new value is the same as the last
      switchMap(name => {
        // Don't validate if the name is empty or hasn't changed from the original in edit mode
        if (!name || (this.isEditMode && name === this.originalName)) {
          this.employeeNameExists = false;
          this.cdr.markForCheck();
          return of([]); // Return empty array for autocomplete
        }
        return this.employeeService.getEmployeesByNameSubstring(name).pipe(
          tap(employees => {
            this.employeeNameExists = employees.some(e => e.name.toLowerCase() === name.toLowerCase());
            this.cdr.markForCheck();
          })
        );
      })
    );
  }

  loadManagers() {
    this.employeeService.getEmployees().subscribe((allEmployees: Employee[]) => {
      // A manager is an employee who is a root employee (no manager) or has the `hasManagerRights` flag set.
      const potentialManagers = allEmployees.filter(
        (employee) => !employee.manager || employee.hasManagerRights
      );

      // Ensure the list of managers is unique by employee ID
      const allManagers = potentialManagers.filter((manager, index, self) =>
        index === self.findIndex((m) => m.id === manager.id)
      );

      if (this.isEditMode) {
        // Exclude the current employee from the list of potential managers
        this.managers = allManagers.filter(manager => manager.id !== this.employeeObject.id);
      } else {
        this.managers = allManagers;
      }

      if (this.isEditMode && this.employeeObject.manager) {
        // The manager property can be a full Employee object or just its ID string from the backend.
        const managerId = typeof this.employeeObject.manager === 'object' && this.employeeObject.manager !== null
          ? (this.employeeObject.manager as Employee).id
          : this.employeeObject.manager as any;
        this.employeeObject.manager = this.managers.find(m => m.id === managerId) || null;
      }
    });
  }

  onNameChange(name: string): void {
    this.nameChangeSubject.next(name);
    this.filteredEmployees$ = this.employeeService.getEmployeesByNameSubstring(name);
  }


  compareEmployees(e1: Employee, e2: Employee): boolean {
    return e1 && e2 ? e1.id === e2.id : e1 === e2;
  }

  onSubmit(form: any): void {
    if (form.valid && !this.employeeNameExists) {
      this.dialogRef.close({ ...this.employeeObject });
    }
  }

  getModalTitle(): string {
    return this.isEditMode ? 'edit-employee-title' : 'add-employee-title';
  }


}
