'use strict';
angular.module('loginApp')
    .factory("Auth", Auth)
    .factory("Session",Session)
    .constant('USER_ROLES', {
      	all : '*',
      	admin : 'ADMIN',
      	superadmin : 'SUPERADMIN',
        editor: 'EDITOR',
        guest: 'GUEST'
   })
   .constant('AUTH_EVENTS', {
    	loginSuccess : 'auth-login-success',
    	loginFailed : 'auth-login-failed',
    	logoutSuccess : 'auth-logout-success',
    	sessionTimeout : 'auth-session-timeout',
    	notAuthenticated : 'auth-not-authenticated',
    	notAuthorized : 'auth-not-authorized'
 });
Auth.$inject = ['$resource', '$rootScope', '$window', 'Session', 'AUTH_EVENTS', '$q',"URL", "USER_ROLES", 'cookieService', '$http']
function Auth($resource, $rootScope, $window, Session, AUTH_EVENTS, $q,URL, USER_ROLES, cookieService, $http) {
    var service = {
        login: login,
        isAuthenticated : isAuthenticated,
        isAuthorized : isAuthorized,
        logout : logout
    }
    return service;
    function login(credentials) {
      var provider = "uniapp/authenticate";
      console.log(URL.API_URL+ provider);
      var result = $resource(URL.API_URL + provider, {}, {
        'save':{
          method:'POST',
          isArray:false,
          headers: {
            'Content-Type':'application/json'
          }
        }
      });
      // console.log(result);
      return result;
        // return function() {
        //     return $q(function(resolve, reject) {
        //       $resource(URL.API_URL+ provider)
        //       .save(credentials, function(res) {
        //                 console.log("login Status: ",res);
        //                 if (res.token) {
        //                     // set the browser session, to avoid relogin on refresh
        //                     $window.sessionStorage["userInfo"] = JSON.stringify(credentials);
        //                     $window.sessionStorage["userSessionId"] = res.token;
        //                     // Storing session id in cookies
        //                     //console.log("cookie ",cookieService);
        //                     var user  = {};
        //                     user.username = credentials.username;
        //                     user.sessionId = res.token;
        //                     user.role = USER_ROLES.admin;
        //                     Session.create(user); // sessionId, userId, userRole
        //                     cookieService.put("sessionId", res.token);
        //                     cookieService.put("userName", res.username);
        //                     cookieService.put("role", user.role);
        //                     //console.log("cookie ",cookieService.get("sessionId"));
        //                     resolve("success");
        //                 } else {
        //                     resolve("failure");
        //                 }
        //             },function(err){
        //                 reject("failure")
        //             });
        //     })
        // };
    }

    function isAuthenticated(){
      // return !!Session.user;
      return !!cookieService.get("sessionId");
    }

	//check if the user is authorized to access the next route
	//this function can be also used on element level
	//e.g. <p ng-if="isAuthorized(authorizedRoles)">show this only to admins</p>
	function isAuthorized(authorizedRoles) {
		if (!angular.isArray(authorizedRoles)) {
	      authorizedRoles = [authorizedRoles];
	    }
	    return (isAuthenticated() &&
	    authorizedRoles.indexOf(cookieService.get("role")) !== -1);
	};
  function logout(){
  //  Session.destroy();
  //  $window.sessionStorage.removeItem("userInfo");
  var result = $resource(URL.API_URL + "uniapp/logout", {}, {
    'save':{
      method:'POST',
      isArray:false,
      headers: {
        'Content-Type':'application/json'
      }
    }
  });
  // console.log(result);
  return result;
        // return $q(function(resolve, reject) {
        //   $resource(URL.AUTH_API_URL+ "logout")
        //   .get({}, function(res) {
        //     console.log("in logout");
        //   },function(err){
        //       console.log("errror in logout");
        //       reject("failure")
        //   });
        // })
  }

}
Session.$inject = ['$rootScope','USER_ROLES']
function Session($rootScope,USER_ROLES){
  this.create = function(user) {
		this.user = user;
	};
	this.destroy = function() {
		this.user = null;
	};
	return this;
}
