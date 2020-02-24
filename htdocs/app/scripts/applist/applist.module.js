angular
   .module('applistModule', ['ui.router'
   ])
   .config(applistConfig)
   .run(applistRun);

applistConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider'];
applistRun.$inject = ['$rootScope', '$cookies', '$state'];

function applistConfig($stateProvider, $locationProvider, $urlRouterProvider) {
  $locationProvider.html5Mode({enabled: true,requireBase: false}).hashPrefix("!");

   $stateProvider
       .state("applist",{
          url : '/uniapp/admin/superapplist/:superappid/applist',
           templateUrl :'scripts/applist/applist.html',
           controller:'applistCtrl',
           controllerAs:'vm'
       })
       .state("userapplist",{
         url :'/uniapp/user/superapplist/:superappid/applist',
         templateUrl:'scripts/applist/applist.html',
         controller : 'applistCtrl',
         controllerAs :'vm'
       });
}

function applistRun($rootScope, $cookies, $state) {
  $rootScope.state = $state;
}
