import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataTables2Component } from './data-tables2.component';

describe('DataTables2Component', () => {
  let component: DataTables2Component;
  let fixture: ComponentFixture<DataTables2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTables2Component]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DataTables2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
