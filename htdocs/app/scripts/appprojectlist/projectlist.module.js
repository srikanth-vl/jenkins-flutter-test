angular
   .module('projectlistModule', ['ui.router'
   ])
   .config(projectlistConfig)
   .run(projectlistRun);

projectlistConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider'];
projectlistRun.$inject = ['$rootScope', '$cookies', '$state'];

function projectlistConfig($stateProvider, $locationProvider, $urlRouterProvider) {
  $locationProvider.html5Mode({enabled: true,requireBase: false}).hashPrefix("!");
       $stateProvider
           .state("projects",{
              url : '/uniapp/user/:superappid/:appid/projects',
               templateUrl :'scripts/appprojectlist/projectlist.html',
               controller:'projectlistCtrl',
               controllerAs:'vm'

           })
           
}

function projectlistRun($rootScope, $cookies, $state) {
  $rootScope.state = $state;
}
