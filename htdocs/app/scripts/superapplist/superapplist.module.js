angular
   .module('superapplistModule', ['ui.router'
   ])
   .config(superapplistConfig)
   .run(superapplistRun);

superapplistConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider'];
superapplistRun.$inject = ['$rootScope', '$cookies', '$state'];

function superapplistConfig($stateProvider, $locationProvider, $urlRouterProvider) {
  $locationProvider.html5Mode({enabled: true,requireBase: false}).hashPrefix("!");

   $stateProvider
       .state('superapplist',{
            url : '/uniapp/admin/superapplist',
           templateUrl :'scripts/superapplist/superapplist.html',
           controller:'superapplistCtrl',
           controllerAs:'vm'
       })
       .state('usersuperapplist',{
         url:'/uniapp/user/superapplist',
         templateUrl:'scripts/superapplist/superapplist.html',
         controller:'superapplistCtrl',
         controllerAs:'vm'
       });
}

function superapplistRun($rootScope, $cookies, $state) {
  $rootScope.state = $state;
}
