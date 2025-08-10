
import { Component, Inject, OnInit, Renderer2 } from '@angular/core';
import { DataTablesModule, DataTableDirective } from 'angular-datatables';
import { SuppliersService } from '../service/SuppliersService';
import { Supplier } from '../shared/model/Supplier';
import { CommonModule } from '@angular/common';
import { Config } from 'datatables.net';
import jQuery from 'jquery';
import $ from 'jquery';
import DataTable from 'datatables.net-dt';


@Component({
  selector: 'app-suppliers',
  imports: [],
  templateUrl: './suppliers.component.html',
  styleUrl: './suppliers.component.scss'
})
export class SuppliersComponent implements OnInit {

  public dtOptions: Config = {};
  public suppliersService: SuppliersService = new SuppliersService();
  //  public rendere2: Renderer2 = new Renderer2();


  contructor() {
    console.log("1. CONSTRUTOR")
    // 2public renderer: Renderer2 = new Renderer2
  };


  ngOnInit(): void {
    console.log("2. ngOnIit")
    this.dtOptions = {
      data: this.suppliersService.getSupplierList(),
      lengthMenu: [5, 10, 20, 50],
      columns: [
        { title: 'ID', data: 'id' },
        { title: 'Name', data: 'name' },
        { title: 'Age', data: 'age' }
      ]
    }

    new DataTable($('#t2'), this.dtOptions);

    var myJsonString = JSON.stringify(this.suppliersService.getSupplierList());
    console.log(myJsonString);
  }

}