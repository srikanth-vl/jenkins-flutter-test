import { Injectable } from '@angular/core';
import { Headers, Http, RequestOptions } from '@angular/http';
import { environment } from '../../../environments/environment';
import { Model, Utils } from '../../shared/common/models';

@Injectable()
export class CommService {
  loadedCkeditorJsFile: boolean = false;
  loadedGoogleChartJsFile: boolean = false;
  aemEndpoint: string = 'http://196.12.47.103:4502';

  constructor(
    private http: Http
  ) { }

  // Error handling
  private handleError(error: any) {
    // Handle errors locally if possible
    if (error.status && error.status == 403) {
      console.log("access forbidden");
      // Show some message
    }
    return Promise.reject(error);
  }

  // Get headers
  private getDefaultHeaders() {
    return new Headers(
      {
        'Content-Type': 'application/json',
        'accept': 'application/vnd.api+json'
      });
  }

  // Get headers with access token
  private getHeaders() {
    return new Headers(
      {
        'Content-Type': 'application/json',
        'accept': 'application/vnd.api+json',
        'access-token': localStorage.getItem('access_token')
      });
  }


  // load CKEditor File
  loadCkEditorJSFile() {
    let body = <HTMLDivElement>document.body;
    let script = document.createElement('script');
    script.innerHTML = '';
    script.src = 'https://cdn.ckeditor.com/4.5.11/full/ckeditor.js';
    script.async = true;
    script.defer = true;
    body.appendChild(script);
    this.loadedCkeditorJsFile = true;
    // if (this.loadedCkeditorJsFile) {
    //   return;
    // } else {

    // }
  }
  // load google chart API
  loadGoogleChartJSFile() {
    if (this.loadedGoogleChartJsFile) {
      return;
    } else {
      let body = <HTMLDivElement>document.body;
      let script = document.createElement('script');
      script.innerHTML = '';
      script.src = 'https://www.gstatic.com/charts/loader.js';
      script.async = true;
      script.defer = true;
      body.appendChild(script);
      let loadScript = document.createElement('script');
      loadScript.type = 'text/javascript';
      loadScript.innerText = "google.charts.load('current', {'packages':['corechart']});";
      loadScript.async = true;
      loadScript.defer = true;
      body.appendChild(loadScript);
      this.loadedGoogleChartJsFile = true;
    }
  }
}


