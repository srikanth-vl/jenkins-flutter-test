angular
.module('loginApp')
.controller("loginCtrl",loginCtrl)
.controller("logoutCtrl",logoutCtrl)
.directive('restrictSpecialCharactersDirective', function() {
         function link(scope, elem, attrs, ngModel) {
              ngModel.$parsers.push(function(viewValue) {
                var reg = /^[a-zA-Z0-9]*$/;
                if (viewValue.match(reg)) {
                  return viewValue;
                }
                var transformedValue = ngModel.$modelValue;
                ngModel.$setViewValue(transformedValue);
                ngModel.$render();
                return transformedValue;
              });
          }

          return {
              restrict: 'A',
              require: 'ngModel',
              link: link
          };
  });
loginCtrl.$inject = ['$location','$scope','$state','$window', 'Auth','$rootScope','AUTH_EVENTS','Session','$http', 'cookieService', 'LOGIN_CONSTANTS', 'superappmap'];
logoutCtrl.$inject = ['$scope','$state','Auth','$rootScope','AUTH_EVENTS', 'cookieService', 'LOGIN_CONSTANTS', 'superappmap'];

function loginCtrl($location,$scope,$state,$window,Auth,$rootScope,AUTH_EVENTS,Session,$http,cookieService,LOGIN_CONSTANTS,superappmap){
  console.log("entered into login");
  var vm = this;
  var postdata = {};
  var superappid ='03494406-0e7b-11e9-a47f-0947e4631ca5';
  console.log(superappid);
  postdata.superapp =  superappid;
  $rootScope.loginPage = LOGIN_CONSTANTS.LOGIN;
  vm.credentials = {};
	vm.loginForm = {};
	vm.error = false;
  vm.loginThrobber = false;
  if (cookieService.get(LOGIN_CONSTANTS.SESSION_ID)){
    var user  = {};
    user.username = cookieService.get(LOGIN_CONSTANTS.USER_ID);
    user.sessionid = cookieService.get(LOGIN_CONSTANTS.SESSION_ID);
    user.superapp = cookieService.get(LOGIN_CONSTANTS.SUPER_APP_ID);
    $rootScope.currentUser = user;
    $state.go('userimagecount',{'superappid':'03494406-0e7b-11e9-a47f-0947e4631ca5','appid':'8e54e9aa-508e-337f-bfe7-34e86ba9da27','stdate':'111','eddate':'222'});
  }
  $scope.superappvalues = [
    {superappname:"CADA",superappid:"03494406-0e7b-11e9-a47f-0947e4631ca5"},
    {superappname:"APWRIMS",superappid:"a7cf09ac-43c1-3ba1-aa2d-6a68f786cc44"},
    {superappname:"NOBAGDAY",superappid:"d0bb80aa-bb86-39b6-a351-13f02e72752b"},
  ];

  var url = $location.absUrl().split("/")[4];
  console.log(url);

  vm.login = function(credentials) {
    postdata.mobile=credentials.username;
    postdata.password=credentials.password;
    vm.loginThrobber = true;
    vm.error = false;
    Auth.login()
    .save(postdata,function(data){
      var data = angular.fromJson(angular.toJson(data.result));

      console.log("data is ",data);
      var logindata = data.content;

      if(data.status == 200 && logindata){
        // cookieService.put(LOGIN_CONSTANTS.SUPER_APP_ID, superappid);
        if(url == 'user'){
          cookieService.put(LOGIN_CONSTANTS.SUPER_APP_ID, $location.absUrl().split("/")[5]);
        }else{
          cookieService.put(LOGIN_CONSTANTS.SUPER_APP_ID, superappid);
        }
        cookieService.put(LOGIN_CONSTANTS.USER_ID, logindata.userid );
        cookieService.put(LOGIN_CONSTANTS.SESSION_ID, logindata.tokenid );
        $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
        //$rootScope.page = "nodal";
        var user  = {};
        user.username = cookieService.get(LOGIN_CONSTANTS.USER_ID);
        user.tokenid = cookieService.get(LOGIN_CONSTANTS.SESSION_ID);
        user.superapp = cookieService.get(LOGIN_CONSTANTS.SUPER_APP_ID);
        $rootScope.currentUser = user;
        $rootScope.loginPage = null;

        if(url == 'user'){
          $state.go('userimagecount',{'superappid':'03494406-0e7b-11e9-a47f-0947e4631ca5','appid':'8e54e9aa-508e-337f-bfe7-34e86ba9da27','stdate':'111','eddate':'222'});
        }else{
          $state.go('superapplist');
        }

      } else {
        $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
        vm.error = true;
        $rootScope.loginPage = LOGIN_CONSTANTS.LOGIN;
        vm.loginThrobber = false;
      }
    });

  	  // Auth.login(credentials)().then(function(status){
      //   if (status === "success"){
      //     $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
      //     //$rootScope.page = "nodal";
      //     $rootScope.loginPage = null;
      //     $state.go("nodal",{academicyear : '2017'});
      //   } else {
      //     $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
      //     vm.error = true;
      //     $rootScope.loginPage = "login";
      //     $state.go("login");
      //     vm.loginThrobber = false;
      //   }
      // },function(err){
      //   $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
      //   vm.error = true;
      //   $rootScope.loginPage = "login";
      //   $state.go("login");
      //   vm.loginThrobber = false;
      // })
	};

	if ($window.sessionStorage["userInfo"]) {
  	 	var credentials = JSON.parse($window.sessionStorage["userInfo"]);
  		vm.login(credentials);
	}
}

function logoutCtrl($scope,$state,Auth,$rootScope,AUTH_EVENTS, cookieService,LOGIN_CONSTANTS, superappmap){
  var postdata = {};
  console.log("superappmap ",superappmap);
  var superappid = superappmap.SUPER_APP_ID;
  postdata.userid = cookieService.get(LOGIN_CONSTANTS.USER_ID);
  postdata.token = cookieService.get(LOGIN_CONSTANTS.SESSION_ID);
  postdata.superapp = cookieService.get(superappmap.SUPER_APP_ID) || superappid;
  // console.log("postdata",postdata)
  Auth.logout(postdata)
  .save(postdata,function(data){
    var data = angular.fromJson(angular.toJson(data.result));
    if(data.status == 200){
      $rootScope.currentUser = null;
      $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
      $rootScope.loginPage = LOGIN_CONSTANTS.LOGIN;
      cookieService.removeAll();
      $rootScope.currentUser = false;
      // $state.go("uniapp.login");
      // $state.go("uniapp.home")
      $state.go("uniapp");
      } else {

      }
  });
}
