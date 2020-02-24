import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UniappService } from '../uniapp.service';
import { Superapp } from '../shared/superapp';
import { Child } from '../shared/child';

@Component({
  selector: 'app-superapp-page',
  templateUrl: './superapp-page.component.html',
  styleUrls: ['./superapp-page.component.scss']
})
export class SuperappPageComponent implements OnInit {
  
  constructor(private route: ActivatedRoute,
    private uniappservice: UniappService,
    private router: Router) { }
  superappid: string;
  superapp: Superapp;
  superAppsData: Superapp[] = [];
  private xhr: any = null;
  fetched: boolean = false;
  selectedApp: Child;
  appid = null;
  color = 'accent';
  businessAnalytics = false;
  disabled = false;
  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.superappid = params.get('superappid');})
    if (this.uniappservice.data != null && this.uniappservice.data.length > 0) {
      this.superAppsData = this.uniappservice.data;
      this.superAppsData.forEach(element => {
        if (element['super_app_id'] == this.superappid) {
          this.superapp = element;
          this.fetched = true;
          this.selectSuperapp();
        }
      });

    } else {
      this.xhr = this.uniappservice.fetchSuperApps()
        .subscribe(
          data => {
            this.superAppsData = data;
            this.superAppsData.forEach(element => {
              if (element['super_app_id'] == this.superappid) {
                this.superapp = element;
                this.fetched = true;
                this.selectSuperapp();
              }
            });
          });
    }
  }
  clickApp(app) {
    this.appid = app.app_id;
    this.router.navigate(["/superapp/" + this.superappid + "/" + app.app_id])

  }
  selectSuperapp() {
    this.route.paramMap.subscribe(params => {
      this.appid=  params.get('appid');
      //if(this.appid == "0") {
        // let app  = this.superapp.apps ? this.superapp.apps[0] : null;
        // this.router.navigate(["/superapp/" + this.superappid + "/" + app.app_id])
        // }      
    });
  }
  scroll(el: HTMLElement) {
    el.scrollIntoView();
  }
}
