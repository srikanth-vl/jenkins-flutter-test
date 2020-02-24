import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SuperappPageComponent } from './superapp-page.component';

describe('SuperappPageComponent', () => {
  let component: SuperappPageComponent;
  let fixture: ComponentFixture<SuperappPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SuperappPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SuperappPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
