import { ErrorHandler, Injectable, Injector } from '@angular/core';

// import { AuthService } from '../../login/auth.service';

@Injectable()
export class GlobalErrorHandler extends ErrorHandler {

  constructor(
    private injector: Injector
  ) {
    super();
  }

  handleError(error: any) {



    // Handle expired session token here.
    if (error.status && error.status == 401) {

      // constructor di will not work here.
      // const authService = this.injector.get(AuthService);
      // authService.reAuthenticate();

    }
    super.handleError(error);
  }
}