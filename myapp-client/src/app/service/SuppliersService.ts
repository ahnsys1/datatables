import { Injectable } from "@angular/core";
import { Observable, of } from "rxjs";
import { Supplier } from "../shared/model/Supplier";

@Injectable({
    providedIn: 'root'
})
export class SuppliersService {

    private suppliers: Supplier[] = [
        { id: 1, name: 'Supplier A', age: '9856548956' },
        { id: 2, name: 'Supplier B', age: '9856535269' },
        { id: 3, name: 'Supplier C', age: '9856447856' },
        { id: 4, name: 'Supplier D', age: '9845687900' }
    ]

    public getSupplierList(): Supplier[] {
        return this.suppliers;
    }
}

