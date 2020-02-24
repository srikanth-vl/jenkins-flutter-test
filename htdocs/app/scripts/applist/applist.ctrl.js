(function (){
  angular
  .module('applistModule')
  .controller("applistCtrl", applistCtrl);
  applistCtrl.$inject=['$scope', '$state', '$location', '$http','applistService' ,'uniappDataService', 'cookieService','LOGIN_CONSTANTS','superappmap'];

  function applistCtrl($scope, $state, $location, $http, applistService ,uniappDataService ,cookieService,LOGIN_CONSTANTS,superappmap){
  var vm = this;
  vm.applist = [];
  var postData = {};
   $scope.appList = [];

    postData.userid = superappmap.USER;
    postData.superapp = $state.params.superappid

    postData.token = superappmap.TOKEN;
    if (cookieService.get(LOGIN_CONSTANTS.SESSION_ID)){

       applistService.getForms().save(postData,function(Data){
         console.log(Data);
         var data = angular.fromJson(angular.toJson(Data.result.content.application));
         angular.forEach(data,function(value,key){
             $scope.appList.push({url:value['icon'],name:value['name'],id:value['app_id']});

         });
         var roots = [];
         var all = {};
         data.forEach(function(item){
           all[item.app_id] = item;
         });
         Object.keys(all).forEach(function(val){
           var item = all[val];
           if(item.parent_app_id == $state.params.superappid){
             roots.push(item);
           }
           if(item.parent_app_id in all){
             var p = all[item.parent_app_id];
             if(!('child' in p)){
               p.child = [];
             }
             p.child.push(item);
           }
         });
         console.log(roots);
       });

       $scope.callProjectList = function callProjectList(value){
          var url = $location.absUrl().split("/")[4];
          console.log(url);
          if(url == 'user'){
            $state.go('projects',{'superappid':$state.params.superappid,'appid':value});
          }else{
            $state.go('imagecount',{'superappid':$state.params.superappid,'appid':value});
          }
       };

     } else {
       $state.go("login");
     }

  }
})();
