angular
   .module('detailModule', ['ui.router'])
   .config(projectdetailConfig)
   .run(projectdetailRun);

   projectdetailConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider'];
   projectdetailRun.$inject = ['$rootScope', '$cookies', '$state'];

   function projectdetailConfig($stateProvider, $locationProvider, $urlRouterProvider) {
     $locationProvider.html5Mode({enabled: true,requireBase: false}).hashPrefix("!");
          $stateProvider
              .state("details",{
                url:'/uniapp/user/:superappid/:appid/:projectid/:startDate/:endDate/projectdetails',
                templateUrl:'scripts/appprojectdetails/appprojectdetails.html',
                controller:'projectdetailCtrl',
                controllerAs:'vm'
              })
              .state("imagefile",{
                url:'/uniapp/image/:superappid/:appid/:projectid/:imageid/:lat/:lng',
                // url:'/uniapp/image/',
                templateUrl:'scripts/appprojectdetails/imageview.html',
                controller:'imageCtrl',
                controllerAs:'vm'
              });

   }

   function projectdetailRun($rootScope, $cookies, $state) {
     $rootScope.state = $state;
   }
