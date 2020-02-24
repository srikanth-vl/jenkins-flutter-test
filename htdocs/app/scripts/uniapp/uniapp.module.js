    angular
       .module('uniapp', ['ui.router'
       ])
       .config(uniappConfig)
       .run(uniappRun);

    uniappConfig.$inject = ['$stateProvider', '$locationProvider', '$urlRouterProvider','superappmap'];
    uniappRun.$inject = ['$rootScope', '$cookies', '$state'];

    function uniappConfig($stateProvider, $locationProvider, $urlRouterProvider,superappmap) {
      $locationProvider.html5Mode({enabled: true,requireBase: false}).hashPrefix("!");

       $stateProvider
           .state("uniapp",{
              url : '/uniapp',
               templateUrl :'scripts/uniapp/uniapp.html',
               controller:'uniappCtrl',
               controllerAs:'vm'

           })
           // console.log("projconstants ",superappmap);
           $urlRouterProvider.when('/', '/uniapp');
    }

    function uniappRun($rootScope, $cookies, $state) {
      $rootScope.state = $state;
    }
