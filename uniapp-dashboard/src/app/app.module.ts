import { BrowserModule } from '@angular/platform-browser';import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SuperappComponent } from './superapp/superapp.component';
import { SubappComponent } from './subapp/subapp.component';
import { HttpClientModule } from '@angular/common/http';
import { HighchartsChartModule } from 'highcharts-angular';
import { ReplicateComponent } from './replicate/replicate.component';
import { AngularFireModule } from '@angular/fire';
import { AngularFirestoreModule } from '@angular/fire/firestore';
import { AngularFireDatabaseModule } from '@angular/fire/database';
import { environment } from '../environments/environment';
import { UniappService } from './uniapp.service';
import { GlobalErrorHandler } from './shared/common/global-error-handler';
import { HttpModule } from '@angular/http';
import { SuperappPageComponent } from './superapp-page/superapp-page.component';
import { ChartComponent } from './chart/chart.component';
import { LoginComponent } from './auth/login/login.component';
import { AuthService } from './auth/auth.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material'
import { ProjectSubmissionAnalyticComponent } from './project-submission-analytic/project-submission-analytic.component';
import { ProjectSubmissionDataComponent } from './project-submission-data/project-submission-data.component';
import { BusinessTableComponent } from './business-table/business-table.component';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReplacePipe } from './shared/pipes/replace.pipe';
import { TitleCasePipe } from './shared/pipes/titleCase.pipe';
import { RouterModule } from "@angular/router";

@NgModule({
  declarations: [
    AppComponent,
    SuperappComponent,
    SubappComponent,
    // HighchartsChartComponent,
    ReplicateComponent,
    SuperappPageComponent,
    ChartComponent,
    LoginComponent,
    ProjectSubmissionAnalyticComponent,
    ProjectSubmissionDataComponent,
    BusinessTableComponent,
    ReplacePipe,
    TitleCasePipe
  ],
  imports: [
    BrowserAnimationsModule,
    MatDialogModule,
    BrowserModule,
    MatProgressSpinnerModule,
    AppRoutingModule,
    HttpClientModule,
    AngularFireModule.initializeApp(environment.firebase_production_config),
    AngularFirestoreModule,
    AngularFireDatabaseModule,
    HighchartsChartModule,
    HttpModule,
    CommonModule,
    ReactiveFormsModule,
    MatSlideToggleModule,
    RouterModule
  ],
  providers: [
    UniappService, GlobalErrorHandler,
  ],
  entryComponents: [
    ProjectSubmissionDataComponent
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
