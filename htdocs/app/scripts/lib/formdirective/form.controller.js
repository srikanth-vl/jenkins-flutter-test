(function () {
  angular
  .module('formApp')
  .directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;
            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
  }])
  .controller("formDirectiveController", formDirectiveController);

  formDirectiveController.$inject = ["$scope","$state", "$location", "$http", "formHttpService", "formDataService", "formconstants","cookieService","LOGIN_CONSTANTS",'superappmap'];

  function formDirectiveController($scope, $state, $location, $http, formHttpService, formDataService, formconstants,cookieService,LOGIN_CONSTANTS,superappmap) {

    var vm = this;
    var appid = null;
    $scope.datecheck = 0;
    vm.datedisable = true;
    vm.startdisable = false;
    var currentDate = new Date();
    vm.viewDate = new Date();
    vm.events = [
      {
        startsAt: currentDate,
        endsAt:  new Date(currentDate.getFullYear()+"-"+(currentDate.getMonth() + 1)+"-"+(currentDate.getDate()+1)),
        draggable: true,
        resizable: true,
      }
    ];

    $scope.DateChanged = function(){
      $scope.endDateOptions.minDate = vm.events[0].startsAt;
    }

    $scope.endDateOptions = {
      minDate: new Date(currentDate.getFullYear()+"-"+(currentDate.getMonth() + 1)+"-"+(currentDate.getDate()+1))
    }
    vm.cellIsOpen = true;

    vm.toggle = function($event, field, event) {
      if(field == "startOpen"){
        vm.datedisable = false;
        vm.startdisable = true;
      }
      $event.preventDefault();
      $event.stopPropagation();
      event[field] = !event[field];
    };

    vm.formjsondata = formDataService.prepareFormJsonData($scope.data);
    vm.dropdownlists = vm.formjsondata.dropdownlists;
    vm.formdata = {};
    var postdata = {};
    $scope.superappvalues = formDataService.getsuperappvalues()

      $scope.shutdown = function(){

      $state.go('logout');
    }


    vm.formSubmit = function(buttonType){
      console.log("formdata is ",vm.formdata);

      angular.forEach($scope.appListMap,function(value,key){
        if(value['appname'] == vm.formdata['app']){
          appid = value['appid']
        }
      });

      if(buttonType === formconstants.SUBMIT_BUTTON){
        var formData = {
          'superappid': postdata.superapp,
          "appid":appid,
          "startsAt":new Date(vm.events[0].startsAt),
          "endsAt":new Date(vm.events[0].endsAt)
        };
        console.log("In form controller: ",formData);

        $scope.onSubmit({response: formData});
      }
    }

    vm.changeOnDropdownSelect =  function(formfield){
      if(!formfield.change){
        return;
      }

      if(formfield[formconstants.CHILD]){
        var child = vm.formjsondata.fields[formfield[formconstants.CHILD]];
        var parent = child[formconstants.PARENT];
      }else{
        var parent = formfield[formconstants.PARENT];
        var child = formfield[formconstants.PARENT];
      }

      var parentArray = parent.split(",");
      var flag = false;

      angular.forEach(parentArray , function(parent){
        if(vm.formdata[parent]){
          if(parent == 'superapp'){
            angular.forEach($scope.superappvalues,function(value,key){
              if(vm.formdata[parent] == value.superappname){
                postdata[parent] = value.superappid;
              }
            });
          }

          flag = true;
        }
        else{
          flag = false;
        }
      });
      if(!flag){
        return;
      }

      postdata.token = superappmap.TOKEN;
      postdata.userid = superappmap.USER;

      var listArrayUrl = child[formconstants.LISTARRAYURL];
      if(child[formconstants.ISARRAY] == true){
        formHttpService.getDataAsArray(listArrayUrl)
        .save(postdata,function(data){
          data = angular.fromJson(angular.toJson(data));
          vm.dropdownlists[child[formconstants.KEY]] = data;
        });
      }
      else if(child[formconstants.ISARRAY] == false){
        formHttpService.getDataAsObject(listArrayUrl)
        .save(postdata,function(data){
          console.log(data);
          getArrayData(data,formfield.key,child);
        });
      }
    }
    function getArrayData(data,key,child){
      $scope.appList = [];
      $scope.appListMap = [];
      var Data = angular.fromJson(angular.toJson(data.result.content.application));
      angular.forEach(Data,function(value,key){
        $scope.appList.push(value['name']);
        $scope.appListMap.push({appname:value['name'],appid:value['app_id']});
      });
      console.log($scope.appListMap);
      vm.dropdownlists[child[formconstants.KEY]] = $scope.appList;
    }
  }
})();
