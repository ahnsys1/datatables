import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { Employee } from '../shared/model/Employee';


@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  API_URL = "http://localhost:9090";

  constructor(private http: HttpClient) { }


  getEmployee(id: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.API_URL}/employees/${id}`);
  }

  isEmployeeExists(name: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/employees/exists?name=${name}`);
  }

  getRootEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/employees/tree`);
  }

  getManagers(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/employees/managers`);
  }

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/employees`);
  }

  getEmployeesWithManagers(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/employees/with-managers`);
  }

  employeeByNameExists(employeeName: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/employees/employeeByNameExists/${employeeName}`);
  }

  createEmployee(employee: Employee): Observable<Employee> {
    let params = new HttpParams();
    const managerId = employee.manager ? employee.manager.id : null;
    if (managerId) {
      params = params.append('managerId', managerId);
    }
    return this.http.post<Employee>(`${this.API_URL}/employees`, employee, { params });
  }



  getEmployeesByNameSubstring(name: string): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/employees/getEmployeesByNameSubstring/search?name=${encodeURIComponent(name)}`);
  }


  updateEmployee(employee: Employee): Observable<Employee> {
    let params = new HttpParams();
    // When updating, we always specify the manager's status.
    // If employee.manager is an object, we use its ID to set/change the manager.
    // If employee.manager is null, it signifies that the manager should be removed.
    // The backend expects a 'null' string to process the removal.
    const managerId = employee.manager ? employee.manager.id : 'null';
    params = params.append('managerId', managerId);
    return this.http.put<Employee>(`${this.API_URL}/employees/${employee.id}`, employee, { params });
  }

  deleteEmployee(empId: string): Observable<void> {
    const deleteUrl = `${this.API_URL}/employees/${empId}`;
    //   alert(deleteUrl);
    return this.http.delete<void>(deleteUrl);
  }

}