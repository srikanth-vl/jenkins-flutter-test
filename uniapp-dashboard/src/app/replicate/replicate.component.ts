import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Superapp } from '../shared/superapp';
import { UniappService } from '../uniapp.service';


@Component({
  selector: 'app-replicate',
  templateUrl: './replicate.component.html',
  styleUrls: ['./replicate.component.scss']
})
export class ReplicateComponent implements OnInit {

  constructor(
    private uniappService: UniappService
  ) { }
  rootJson;
  superapps: Superapp[] = [];
  private xhr: any = null;
  ngOnInit() {
    this.xhr = this.uniappService.fetchSuperApps()
      .subscribe(
        data => {
          this.superapps = this.uniappService.data;
        },
        (err: HttpErrorResponse) => {
          console.log(err.message);
        }
      );
  }
}
