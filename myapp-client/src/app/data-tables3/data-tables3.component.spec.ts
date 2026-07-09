import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataTables3Component } from './data-tables3.component';

describe('DataTables3Component', () => {
  let component: DataTables3Component;
  let fixture: ComponentFixture<DataTables3Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTables3Component]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DataTables3Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
