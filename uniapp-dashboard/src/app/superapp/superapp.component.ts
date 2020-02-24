import { Component, OnInit, Input, Output } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Superapp } from '../shared/superapp';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-superapp',
  templateUrl: './superapp.component.html',
  styleUrls: ['./superapp.component.scss']
})
export class SuperappComponent implements OnInit {
  title = 'hierarchy';
  @Input() superApp: Superapp;
  // @Output() click;
  constructor(private httpService: HttpClient,
    private router: Router) { }
  hierJson: object[];
  apps: object[];

  ngOnInit() {

    let name = this.superApp['super_app_name'];
    this.apps = this.superApp['apps'];
  }

  displaySuperapp() {
    let supapp = this.superApp.super_app_id;
    this.router.navigateByUrl('/superapp/' + supapp + "/0");
  }

  scroll(el: HTMLElement) {
    el.scrollIntoView();
  }
}
