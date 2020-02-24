  angular
     .module('translogdashboard', ['ui.router'
     ])
     .config(translogdashboardConfig)
     .run(translogdashboardRun);

  translogdashboardConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider'];
  translogdashboardRun.$inject = ['$rootScope', '$cookies', '$state'];

  function translogdashboardConfig($stateProvider, $locationProvider, $urlRouterProvider) {
      //console.log("coming");

    $locationProvider.html5Mode({enabled: true, requireBase: false}).hashPrefix("!");


    $stateProvider
      .state("uniapp.translogdashboard",{
        url : "/translogdashboard/:superappid/:appid/:userid",
        templateUrl : "scripts/translogdashboard/translogdashboard.html",
        controller : "translogdashboardCtrl",
        controllerAs : "vm"
      })

  }

  function translogdashboardRun($rootScope, $cookies, $state) {
    $rootScope.state = $state;
    //console.log($state);
  }
