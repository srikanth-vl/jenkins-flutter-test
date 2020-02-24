import { Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material';  
@Component({
  selector: 'app-project-submission-data',
  templateUrl: './project-submission-data.component.html',
  styleUrls: ['./project-submission-data.component.scss']
})
export class ProjectSubmissionDataComponent {

submissiondata = []
  constructor(@Inject(MAT_DIALOG_DATA) public data:any){ 
     this.submissiondata = data;
     this.submissiondata.forEach(element => {
      element["submitted_data"]=JSON.stringify(element["submitted_data"]);
     });
  }  
  

}
