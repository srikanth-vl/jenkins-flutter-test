import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectSubmissionDataComponent } from './project-submission-data.component';

describe('ProjectSubmissionDataComponent', () => {
  let component: ProjectSubmissionDataComponent;
  let fixture: ComponentFixture<ProjectSubmissionDataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectSubmissionDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectSubmissionDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
