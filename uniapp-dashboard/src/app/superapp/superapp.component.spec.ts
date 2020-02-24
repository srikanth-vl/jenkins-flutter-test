import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SuperappComponent } from './superapp.component';

describe('SuperappComponent', () => {
  let component: SuperappComponent;
  let fixture: ComponentFixture<SuperappComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SuperappComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SuperappComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
