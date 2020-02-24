angular
   .module('imageCount', ['ui.router'
   ,'multipleDatePicker', 'ui.bootstrap'])
   .config(imageCountConfig)
   .run(imageCountRun);

imageCountConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider'];
imageCountRun.$inject = ['$rootScope', '$cookies', '$state'];

function imageCountConfig($stateProvider, $locationProvider, $urlRouterProvider) {

  $locationProvider.html5Mode({enabled: true, requireBase: false}).hashPrefix("!");

  $stateProvider
    .state("imagecount",{
      url : "/uniapp/admin/:superappid/:appid/imagecount",
      templateUrl : "scripts/imagecount/imagecount.html",
      controller : "imageCountCtrl",
      controllerAs : "vm"
    })
    .state("userimagecount",{
      url:"/uniapp/user/:superappid/:appid/:stdate/:eddate/imagecount",
      templateUrl:"scripts/imagecount/imagecount.html",
      controller:"imageCountCtrl",
      controllerAs:"vm"
    });
}

function imageCountRun($rootScope, $cookies, $state) {
  $rootScope.state = $state;
  //console.log($state);
}
