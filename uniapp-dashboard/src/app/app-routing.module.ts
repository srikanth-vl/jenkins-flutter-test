import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ReplicateComponent } from './replicate/replicate.component';
import { SuperappPageComponent } from './superapp-page/superapp-page.component';
import { LoginComponent } from './auth/login/login.component';
import { AuthGuard } from './auth/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: ReplicateComponent, canActivate: [AuthGuard] },
  { path: 'superapp/:superappid/:appid', component: SuperappPageComponent, canActivate: [AuthGuard] },
  ];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
