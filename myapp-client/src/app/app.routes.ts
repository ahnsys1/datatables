import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { DataBindingComponent } from './data-binding/data-binding.component';
import { SuppliersComponent } from './suppliers/suppliers.component';
import { TodoListComponent } from './todo-list/todo-list.component';
import { DataTables2Component } from './data-tables2/data-tables2.component';
import { DataTables3Component } from './data-tables3/data-tables3.component';
import { AddEmployeeComponent } from './add-employee/add-employee.component';
import { ProfileComponent } from './profile/profile.component';
import { TreesOfEmployeesComponent } from './trees-of-employees/trees-of-employees.component';

export const routes: Routes = [
    { path: 'datatable1', component: DataTables2Component },
    { path: 'datatable2', component: DataTables3Component },
    { path: 'profile', component: ProfileComponent },
    { path: 'todo-list', component: TodoListComponent },
    { path: 'suppliers', component: SuppliersComponent },
    { path: 'data-binding', component: DataBindingComponent },
    { path: 'trees-of-employees', component: TreesOfEmployeesComponent }
];