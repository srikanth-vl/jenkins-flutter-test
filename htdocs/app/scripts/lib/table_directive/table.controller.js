(function() {
angular
    .module('vassarTable')
    .controller('vassarTableDirectiveController', vassarTableDirectiveController)
    .controller('gettableController',gettableController)
    .filter("vassarTableSorting",vassarTableSorting);

function vassarTableSorting(){
  return function(items, field, reverse) {

    var filtered = [];
    angular.forEach(items, function(item) {
      filtered.push(item);
    });
    field = parseFloat(field);
    if(filtered[0] !==undefined && filtered[0].length > field){
    filtered.sort(function (a, b) {
     var v1,v2;

     if(a[field]!=undefined && b[field]!=undefined){
     if(a[field] instanceof Array || b[field] instanceof Array ){
         v1 = a[field][0].value;
         v2 = b[field][0].value;
      }else {
        v1 = a[field].value;
        v2 = b[field].value;
      }
      if (isNaN(v1) === false || isNaN(v2) === false) {
        return (parseFloat(v1) > parseFloat(v2)) ? -1 : 1;
      }

      if(v1!=undefined && v2!=undefined){
       if(v1 ==="OTHERS"){
         return 1;
       }
        if(v2 === "OTHERS"){
           return -1;
         }

        var ax = [], bx = [];
        var a = v1;
        var b = v2;
        a.replace(/(\d+)|(\D+)/g, function(_, $1, $2) { ax.push([$1 || Infinity, $2 || ""]) });
        b.replace(/(\d+)|(\D+)/g, function(_, $1, $2) { bx.push([$1 || Infinity, $2 || ""]) });

        while(ax.length && bx.length) {
            var an = ax.shift();
            var bn = bx.shift();
            var nn = (an[0] - bn[0]) || an[1].localeCompare(bn[1]);
            if(nn) return nn;
        }
        return ax.length - bx.length;

      }
    }
    });
   }
    if(reverse)
      filtered.reverse();
    return filtered;
  };
}
vassarTableDirectiveController.$inject = ['$scope', '$state', '$stateParams', '$filter',"$uibModal"];
function vassarTableDirectiveController($scope, $state, $stateParams, $filter,$uibModal) {


  $scope.submitImage = function(image){
    console.log("callback in controller");
    $scope.callback({data: image});
  }

  $scope.showJsonFormat = function(value) {
    console.log(typeof value, value);
    temp = $filter('json')(value, '\n');
    console.log(temp);
    return temp;

  }
/*FUNCTION FOR DATATYPE "CLICKNUMBER"*/
// url data.
   $scope.submitData = function (rowIndex,value, value1){
/* EMIT EVENT - PARENT CONTROLLER WILL DO THE TASK */
    /*var event_data = {};
     $scope.$emit("clickEvent", event_data);*/

/*OR DO REQUIRED TASK IN THIS CONTROLLER ONLY*/
      var tableData = $scope.data;
      var data = $filter('vassarTableSorting')(tableData.recordData, tableData.sortIndex, tableData.sortOrder);
      var clickColRow = data[rowIndex][4];
      console.log("clickColRow is---",clickColRow);
      console.log("the value is-",value);
      var parent = clickColRow.parent;
      var location = clickColRow.location;
      var child = clickColRow.child;
      if(location){
        var sparent = parent;
        var slocation = location;
        var view  = $state.params.view;
        var parentArray = $state.params.parent.split("&");
        var locationArray = $state.params.location.split("&");
        if($state.params.parent){
          var index = parentArray.indexOf(parent);
          if(index > 0){
            parentArray[index] = parent;
            locationArray[index] = location;
          }else {
            parentArray.push(parent);
            locationArray.push(location);
          }
          sparent =  parentArray.join("&");
        }
        if($state.params.location){
          slocation = locationArray.join("&");
        }
      }

      // url click data;
      // url = value.clickData || "";
      var url = "";
      if(value1){
        url = value1;
      }

      if(url == null || url == undefined || url == "" || url.type == "popup"){
        var data = {
          'type': 'popup',
          'row':clickColRow
        }
        console.log("coming in to this");
        $scope.callback({data: data});
      }else{
        console.log("change state accroding to params");
      }
      // var modalInstance = $uibModal.open({
      //
      //   templateUrl: 'scripts/demoTable/modal.html',
      //   resolve: {
      //     data: function () {
      //       var dataObject = {};
      //        dataObject.view = view;
      //       dataObject.child = child;
      //       dataObject.parent = sparent;
      //       dataObject.location = slocation;
      //       dataObject.value = value;
      //       dataObject.url = url;
      //       return dataObject;
      //     }
      //   },
      //   controller: "gettableController",
      //   size: "lg",
      //   backdrop: 'static',
      // });



   }


    $scope.submit = function (child,parent,location) {
      if(location){
            var sparent = parent;
            var slocation = location;
            var view  = $state.params.view;
            var parentArray = $state.params.parent.split("&");
            var locationArray = $state.params.location.split("&");
            if($state.params.parent){
              var index = parentArray.indexOf(parent);
              if(index > 0){
                parentArray[index] = parent;
                locationArray[index] = location;
              }else {
                parentArray.push(parent);
                locationArray.push(location);
              }
              sparent =  parentArray.join("&");
            }
            if($state.params.location){
               slocation = locationArray.join("&");
            }
        $state.go($state.current.name, {view: view,child: child,parent:sparent,location:slocation});
      }
    };

    $scope.range = function(count){
      var ratings = [];
      for (var i = 0; i < count; i++) {
        ratings.push(i)
      }
      return ratings;
    }

    $scope.submitStudent = function (child,parent,location) {
      if(location){

            var sparent = parent;
            var slocation = location;
            var view  = $state.params.view;
            var parentArray = $state.params.parent.split("&");
            var locationArray = $state.params.location.split("&");
            var examtype = $state.params.examtype;
            var examCode = $state.params.exam;
            var star = $state.params.star;
            var subject = $state.params.subject;
            var academicyear = $state.params.academicyear;
            var medium = $state.params.medium;
            if($state.params.parent){
              var index = parentArray.indexOf(parent);
              if(index > 0){
                parentArray[index] = parent;
                locationArray[index] = location;
              }else {
                parentArray.push(parent);
                locationArray.push(location);
              }
              sparent =  parentArray.join("&");
            }
            if($state.params.location){
               slocation = locationArray.join("&");
            }
        $state.go("dashboard.reportcard.child", {academicyear : academicyear, view: view,child: child,parent:sparent,location:slocation,examtype:examtype,medium : medium,exam : examCode,star:star, subject:subject});
      }
    };

      $scope.send = function (child,parent,location) {
            $state.go($state.current.name, {child:child,parent: parent,location:location});
      };
      $scope.setSortOrder = function(index){
        if(index){
          if ($scope.data.sortIndex === index) {
  					$scope.data.sortOrder = !$scope.data.sortOrder;
  				} else {
  					$scope.data.sortOrder = false;
  				}
  				$scope.data.sortIndex = index;
  			}
      };



}

gettableController.$inject = ["$uibModal","$uibModalInstance", 'data', '$scope','table_dataconstant', 'vassarTableFormatService','commonDataService','demoTableHttpService','demoTableDataService'];

function gettableController($uibModal,$uibModalInstance, data, $scope,table_dataconstant, vassarTableFormatService,commonDataService,demoTableHttpService,demoTableDataService) {

    /*YOUR CODE GOES HERE*/
    var records=[];
 var postData={};
 $scope.demoTable = {};
 console.log("data on click is----",data);
 var parentLocationId = commonDataService.getNthParentLocationId(data.location, 1);
 // var grandParentLocationId = commonDataService.getNthParentLocationId(data.location, 2);
 // var greatGrandParentLocationId = commonDataService.getNthParentLocationId(data.location, 3);
 var parentLocation = commonDataService.getNthParentLocationName(data.parent, 1);
 postData.child = data.child;
 postData.parent = data.parent;
 postData.location = parentLocation;
 postData.locationId = parentLocationId;
 postData.academicyear = commonDataService.calulateAcademicyear(new Date());
 postData.examcode = "CFC-CPT 1";
 postData.examtype = "CFC";
 console.log("postdata is----------",postData);
 $scope.table1Trobber = true;
 var tableName = postData.examtype == "AFC" ? "afcTable" : "getTable";
 demoTableHttpService.geturldata(data.url)
 .save(postData,function(data){
   var finalArr = [];
   angular.forEach(data, function(value, key){
         finalArr.push(angular.fromJson(value));
   });
   $scope.tableData = demoTableDataService.formatTable(finalArr,tableName,postData.child);

   console.log("vm.tableData after click is-",$scope.tableData);
   $scope.table1Trobber = false;
 });

    $scope.close = function(){
        $uibModalInstance.close();
      }
  }
})();
