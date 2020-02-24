import { Injectable } from '@angular/core';
import { GlobalErrorHandler } from './shared/common/global-error-handler';
import { Headers, Http, RequestOptions, ResponseContentType } from '@angular/http';

import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';
import { Superapp } from './shared/superapp';
import { AuthService } from './auth/auth.service';


@Injectable()
export class UniappService {

  constructor(
    private http: Http,
    private app: GlobalErrorHandler,
    private authService: AuthService
  ) { }
  public data = [];

  private handleError(error: any) {
    // Handle errors locally if possible
    if (error.status && error.status == 403) {
      console.log("access forbidden");
      // Show some message
    }
    // If able to solve
    // return Promise.resolve();

    // let global error handler takeover all generic errors
    // this.globalErrorHandler.handleError(error);
    return Promise.reject(error);
  }

  private getHeaders() {
    return new Headers(
      {
        'Content-Type': 'application/json',
        'accept': 'application/vnd.api+json'

      });
  }

  public getSuperApps(): Promise<any> {

    let url = environment.BASE_URL + '/superapplist';
    console.log("http");
    console.log(this.http)
    return this.http.get(url, new RequestOptions({ 'headers': this.getHeaders() }))
      .toPromise()
      .then(response => {
        let resp = response.json();
        console.log(resp);
        // return {'assets': r, 'count': resp.meta.count};
      })
      .catch(e => this.handleError(e));
  }

  tokenExpiry(): Observable<any> {

    let url = environment.BASE_URL + 'dashboard/tokenexpiry';

    let user = JSON.parse(localStorage.getItem('currentUser'));
    let userId: string = user.userId;
    let token: string = user.token;
    let body = { user: userId, token: token };

    return this.http.post(url, body, new RequestOptions({ 'headers': this.getHeaders() })).pipe(map(resp => {
      console.log(resp);

      if (resp.json() != null && resp.json()['result'] != null && resp.json()['result']['content'] != null) {
        console.log("Token Expired");
        this.authService.logout();
      } else {
        return null;
      }
    }))
  }

  // This method is same as getAssets except it return observable rather than promise.
  fetchSuperApps(): Observable<any> {

    let url = environment.BASE_URL + 'dashboard/displayroot';
    let userId = JSON.parse(localStorage.getItem('currentUser')).userId;
    let body = { user: userId };

    return this.http.post(url, body, new RequestOptions({ 'headers': this.getHeaders() })).pipe(map(resp => {
      // console.log(resp.json());
      if (resp.json() != null && resp.json()['result'] != null && resp.json()['result']['content'] != null) {
        this.data = resp.json()['result']['content'];
        let rootJson = resp.json()['result']['content'];
        let ell;
        let superapps: Superapp[] = [];
        rootJson.forEach(element => {
          ell = new Superapp;
          ell = <Superapp>element;
          superapps.push(ell);
        });
        this.createheirarchy(superapps);
        return superapps;
      } else {
        return null;
      }
    }))

  }
  createheirarchy(superapps) {
    superapps.forEach(superapp => {
      if (superapp['app_analytics'] != undefined && superapp['app_analytics'] != null
        && superapp['app_analytics'].length != 0) {
        superapp.apps = [];
        superapp['app_analytics'].forEach(child => {
          if (child['parent_id'] == superapp.super_app_id) {
            superapp.apps.push(child);
            this.getChilds(superapp['app_analytics'], child);
          }
        });
      }
    });
    this.data = superapps;
  }

  getChilds(list, app) {
    app.child = [];
    list.forEach(element => {
      if (element['parent_id'] == app['app_id']) {
        app.child.push(element);
        this.getChilds(list, element);
      }
    });
  }
  public getProjectSubmissionAnalytics(superapp, app, attributeName, attributeValue): Observable<any> {

    let url = environment.BASE_URL + 'projectsubmissionanalytics';
    let body = {
      super_app: superapp,
      app_id: app,
      parent_entity_name: attributeName,
      parent_entity_value: attributeValue
    }

    return this.http.post(url, body, new RequestOptions({ 'headers': this.getHeaders() })).pipe(map(resp => {
      // console.log(resp.json());
      if (resp.json() != null && resp.json()['result'] != null && resp.json()['result']['content'] != null) {
        // console.log(resp.json()['result']['content']);
        return resp.json()['result']['content'];
      } else {
        return null;
      }
    }))

  }
  public getProjectsubmissiondata(superapp, app ,projectids): Observable<any> {

    let url = environment.BASE_URL + 'projectsubmissiondata';
    let body = {
      super_app_id: superapp,
      app_id: app,
      project_ids: projectids,
    }

    return this.http.post(url, body, new RequestOptions({ 'headers': this.getHeaders() })).pipe(map(resp => {
      // console.log(resp.json());
      if (resp.json() != null) {
        console.log(resp.json());
        return resp.json();
      } else {
        return null;
      }
    }))
  }
    public getBusinessAnalyticsData(superapp, app, attributeName, attributeValue): Observable<any> {

      let url = environment.BASE_URL + 'dashboard/businessdata';
      let body = {
        super_app: superapp,
        app_id: app,
        parent_entity_name: attributeName,
        parent_entity_value: attributeValue
      }
  
      return this.http.post(url, body, new RequestOptions({ 'headers': this.getHeaders() })).pipe(map(resp => {
        // console.log(resp.json());
        if (resp.json() != null && resp.json()['result'] != null && resp.json()['result']['content'] != null) {
          // console.log(resp.json()['result']['content']);
          return resp.json()['result']['content'];
        } else {
          return null;
        }
      }))
  }
}