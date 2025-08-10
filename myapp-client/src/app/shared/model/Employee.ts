

export class Employee {
    public id: string;
    public name: string;
    public position: string;
    public salary: string;
    public start_date: string;
    public office: string;
    public extn: string;

    public manager: Employee | null = null;
    public hasManagerRights: boolean = false;
    public children: Employee[] = [];
    constructor(
        id: string,
        name: string,
        position: string,
        salary: string,
        start_date: string,
        office: string,
        extn: string,
        hasManagerRights: boolean = false,
        manager: Employee | null = null
    ) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
        this.start_date = start_date;
        this.office = office;
        this.extn = extn;
        this.hasManagerRights = hasManagerRights;
        this.manager = manager;
    }
}