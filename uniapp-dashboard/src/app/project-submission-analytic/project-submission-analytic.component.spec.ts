import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectSubmissionAnalyticComponent } from './project-submission-analytic.component';

describe('ProjectSubmissionAnalyticComponent', () => {
  let component: ProjectSubmissionAnalyticComponent;
  let fixture: ComponentFixture<ProjectSubmissionAnalyticComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectSubmissionAnalyticComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectSubmissionAnalyticComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
