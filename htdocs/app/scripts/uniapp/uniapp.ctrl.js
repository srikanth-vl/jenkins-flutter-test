(function (){
  angular
  .module('uniapp')
  .controller("uniappCtrl", uniappCtrl);
  uniappCtrl.$inject=['$scope', '$rootScope', '$state', '$location', '$http', 'uniappDataService', 'cookieService'];

  function uniappCtrl($scope,$rootScope, $state, $location, $http, uniappDataService, cookieService){
    var vm  =this;
    var postData = {}

    $scope.adminLogin = function(){
      $state.go('login');
    }
    $scope.userLogin = function(){
      $state.go('userlogin',{'superappid':'03494406-0e7b-11e9-a47f-0947e4631ca5'});
    }
    $scope.fileUpload = function(){
      $state.go('usersuperapplist');
    }

  }

})();
