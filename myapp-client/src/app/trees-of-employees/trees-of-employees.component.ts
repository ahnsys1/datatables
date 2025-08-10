import { Component, OnInit } from '@angular/core';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CommonModule } from '@angular/common';
import { Employee } from '../shared/model/Employee';
import { EmployeeService } from '../service/EmployeeService';
import { SelectionModel } from '@angular/cdk/collections';

@Component({
  selector: 'app-trees-of-employees',
  standalone: true,
  imports: [CommonModule, MatTreeModule, MatButtonModule, MatIconModule, MatCheckboxModule],
  templateUrl: './trees-of-employees.component.html',
  styleUrl: './trees-of-employees.component.scss'
})
export class TreesOfEmployeesComponent implements OnInit {
  title = 'Trees of Employees';
  description = 'This component displays the hierarchical structure of employees in a tree format. It allows users to visualize the relationships between employees, such as managers and subordinates.';

  treeControl = new NestedTreeControl<Employee>(node => node.children);
  dataSource = new MatTreeNestedDataSource<Employee>();
  checklistSelection = new SelectionModel<Employee>(true /* multiple */);

  constructor(private employeeService: EmployeeService) {
  }

  ngOnInit(): void {
    // Fetch the hierarchical employee data for the tree from the backend.
    this.employeeService.getRootEmployees().subscribe(employees => {
      this.dataSource.data = employees;
    });
  }

  hasChild = (_: number, node: Employee) => !!node.children && node.children.length > 0;




}


