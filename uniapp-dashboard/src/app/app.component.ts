import { Component } from '@angular/core';
import { UniappService } from './uniapp.service';
import { AuthService } from './auth/auth.service';
import { Event as NavigationEvent } from "@angular/router";
import { filter } from "rxjs/operators";
import { NavigationStart } from "@angular/router";
import { Router } from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'dashboard-app';
  constructor(private uniappService: UniappService,
    private authService: AuthService,
    router: Router) {
  }

  get isLoggedIn() { return this.authService.isLoggedIn(); }

  logout() {
    this.uniappService.tokenExpiry()
      .subscribe(
        data => {
          console.log("logout end");
        },
        (err) => {
          console.log(err.message);
        }
      );

  }
}
