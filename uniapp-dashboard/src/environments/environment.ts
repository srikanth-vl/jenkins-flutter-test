// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  // BASE_URL: "http://localhost:9000/api/uniapp/",
  BASE_URL: "http://uniapp.vassarlabs.com:9003/api/uniapp/",
  // BASE_URL: "http://138.68.30.58:9000/api/uniapp/",
  firebase_production_config :{
    apiKey: "AIzaSyAEBcx4F5fLvaEcobEFb9BkpP9s0xw9SVM",
    databaseURL: "https://prrdfirebase.firebaseio.com",
    authDomain: "prrdfirebase.firebaseapp.com",
    storageBucket: "prrdfirebase.appspot.com",
    projectId: "prrdfirebase",
  },
  firebase_config :{
    apiKey: "AIzaSyDfOPbunt0kTTie_UrE_39AHs0WtYY4P78",
    databaseURL: "https://mytestproject-vassar.firebaseio.com",
    authDomain: "mytestproject-vassar.firebaseapp.com",
    storageBucket: "mytestproject-vassar.appspot.com",
    projectId: "mytestproject-vassar",
  }
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
