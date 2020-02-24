(function (){
  angular
  .module('projectlistModule')
  .controller("projectlistCtrl", projectlistCtrl);
  projectlistCtrl.$inject=['$scope', '$state', '$location', '$http', 'projectlistService', 'cookieService','LOGIN_CONSTANTS','superappmap'];

  function projectlistCtrl($scope, $state, $location, $http, projectlistService, cookieService,LOGIN_CONSTANTS,superappmap){
    var vm = this;
   $scope.loading = true;
   vm.projectList = [];
   vm.fields = [];
    var appid = $state.params.appid;
    var projectData ={};
    var project ;
    projectData.superapp = $state.params.superappid
    projectData.appid = $state.params.appid;
    projectData.userid = superappmap.USER;
    projectData.token = superappmap.TOKEN;
    projectData.formversion = 0;

    projectlistService.getAppForms()
    .save(projectData,function(data){
      var midIntanceIds = [];
      var data = angular.fromJson(angular.toJson(data.result.content));

      var forms = data.content;
      angular.forEach(forms, function (value, key) {

      midIntanceIds.push(value.UPDATE.mdinstanceid)
    });

      projectData.md_instance_id = midIntanceIds;

      projectlistService.getProjects().save(projectData,function(data)
      {
          project = angular.fromJson(angular.toJson(data.result.content.projects));

          for(var i=0;i<project.length;i++)
          {
              vm.projectList.push(project[i]);
              if($location.absUrl().split("/")[8] == 'details'){
                if($location.absUrl().split("/")[7] == project[i].projectid){
                  vm.fields.push(project[i].fields);
                  console.log(vm.fields);
                }
              }
          }
      });
         console.log(vm.projectList);
         $scope.projectdetails = function(val){
           $state.go('projectdetails',{'superappid':projectData.superapp,'appid':projectData.appid,'projectid':val});

         }
  });
}
})();
