angular
   .module('loginApp', ['ui.router',"ngResource","urlApp", 'cookieModule'])
   .config(loginConfig)
   .run(loginrun);
loginConfig.$inject = ['$stateProvider','$urlRouterProvider','$locationProvider'];
function loginConfig($stateProvider, $urlRouterProvider,$locationProvider){
  $stateProvider
    .state('login', {
         url: '/admin/login',
         templateUrl: "scripts/login/login.html",
         controller: "loginCtrl",
         controllerAs: "vm"
     })
     .state('logout', {
          url: '/uniapp/logout',
          controller: "logoutCtrl",
          controllerAs: "vm"
      }).state('userlogin',{
          url : '/uniapp/user/login',
          templateUrl:"scripts/login/login.html",
          controller:"loginCtrl",
          controllerAs:"vm"
      });

}
loginrun.$inject = ['$rootScope', '$state', 'Auth', 'AUTH_EVENTS'];
function loginrun($rootScope, $state, Auth, AUTH_EVENTS){

       //before each state change, check if the user is logged in
      //and authorized to move onto the next state
   //    $rootScope.$on('$stateChangeStart', function (event, next) {
   //
   //      /*if($rootScope.unAuthorized != null && $rootScope.unAuthorized != undefined && $rootScope.unAuthorized == true){
   //        $state.go("logout");
   //      }*/
   //        if(!!next.data){
   //          //console.log("callled");
   //          var authorizedRoles = next.data.authorizedRoles;
   //          //console.log("authorized roles ",authorizedRoles);
   //          if (!Auth.isAuthorized(authorizedRoles)) {
   //              //console.log("called inside this!!");
   //              event.preventDefault();
   //            if (Auth.isAuthenticated()) {
   //              // user is not allowed
   //              $rootScope.$broadcast(AUTH_EVENTS.notAuthorized);
   //            } else {
   //              // user is not logged in
   //              $rootScope.$broadcast(AUTH_EVENTS.notAuthenticated);
   //            }
   //            $state.go("uniapp.login");
   //            //$rootScope.page="login";
   //          }
   //     }
   //
   //
   //
   // });
}
