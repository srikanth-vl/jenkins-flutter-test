import { Component, OnInit, Input, OnChanges } from '@angular/core';
import { UniappService } from '../uniapp.service';
import { Superapp } from '../shared/superapp';
import { app } from 'firebase';

import { RouterModule, Router } from '@angular/router';

import { ActivatedRoute } from '@angular/router';
import { Child } from '../shared/child';
import { Body } from '@angular/http/src/body';
import { MatDialog } from '@angular/material';
import { ProjectSubmissionDataComponent } from '../project-submission-data/project-submission-data.component';

@Component({
  selector: 'app-business-table',
  templateUrl: './business-table.component.html',
  styleUrls: ['./business-table.component.scss']
})
export class BusinessTableComponent implements OnInit {

  ngOnChanges(): void {
  }
  xhr;

  constructor(
    private uniappService: UniappService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog
  ) { }

  @Input() superAppId;
  @Input() appId;
  @Input() superapp: Superapp;
  app: Child;
  @Input() parent;
  paramsKey = [];
  submissionCount = []
  attributeName: String = null;
  attributeValue: String = null;
  fetched: boolean = false;
  clickable = true;
  location_herierchy = {};
  ngOnInit() {

    this.route.paramMap.subscribe(params => {
      let appid = params.get('appid');
      this.paramsKey = params.keys;
      if (appid == "0") {
        this.app = this.superapp.apps ? this.superapp.apps[0] : null;
      }
      else {
        this.app = this.superapp.apps.find(a => a.app_id == appid);
      }
  this.location_herierchy = {};
      this.paramsKey.forEach(element => {
        if (this.app.attribute_heirarchy.indexOf(element) >= 0) {
          this.attributeName = element;
          this.attributeValue = params.get(element);
          this.location_herierchy[this.attributeName+""] = this.attributeValue;
        } else {
          this.attributeName = null;
          this.attributeValue = null
        }
      });
      if (this.attributeName && this.app.attribute_heirarchy && this.app.attribute_heirarchy.indexOf(this.attributeName) == this.app.attribute_heirarchy.length - 1) {
        this.clickable = false;
      } else {
        this.clickable = true;
      }

      this.fetchData();
    });

  }
  fetchData() {
    this.xhr = this.uniappService.getBusinessAnalyticsData(this.superAppId, this.app.app_id, this.attributeName, this.attributeValue)
      .subscribe(
        data => {
          if (this.attributeName && this.app.attribute_heirarchy && this.app.attribute_heirarchy.indexOf(this.attributeName) == this.app.attribute_heirarchy.length - 2) {
            this.clickable = false;
          } else {
            this.clickable = true;
          }
          this.submissionCount = data;
          if (this.submissionCount != null && this.submissionCount != undefined && this.submissionCount.length > 0) {
            this.attributeName = this.submissionCount[0]["entity_name"];
          }
          this.fetched = true;
        },
        (err) => {
          console.log(err.message);
        }
      );

  }
  fetchDataForEnity(entityValue, row) {
    if (this.app.attribute_heirarchy.indexOf(this.attributeName) == this.app.attribute_heirarchy.length - 1) {
      return;
    }
    this.fetched = false;
    this.attributeValue = entityValue;
    var body = {};
    body[this.attributeName + ""] = entityValue;
    this.location_herierchy[this.attributeName + ""] = entityValue;
    this.router.navigate(["superapp/" + this.superAppId + "/" + this.app.app_id, this.location_herierchy])
  }
  navigateTo(element){
    let body = {};
    this.app.attribute_heirarchy.forEach(e => {
      if(!body[element+""]) {
        body[e+""] = this.location_herierchy[e+""];
      }
    });
    this.router.navigate(["/superapp/" + this.superAppId + "/" + this.app.app_id, body]);
  }
  navigate() {
    this.router.navigate(["/superapp/" + this.superAppId + "/" + this.app.app_id]);
  }
}
