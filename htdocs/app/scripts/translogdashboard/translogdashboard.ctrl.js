+(function (){
  angular
  .module('translogdashboard')
  .controller("translogdashboardCtrl", translogdashboardCtrl);
  translogdashboardCtrl.$inject=['$scope','$state','$location','$http','translogdashboardDataService', 'translogdashboard_table_md', 'translogdashboardHttpService'];

  function translogdashboardCtrl($scope, $state, $location, $http, translogdashboardDataService, translogdashboard_table_md, translogdashboardHttpService){
    // console.log("coming to controller");
    var vm = this;
    var postData = {};
    var tableName = "translogdashboardTable";
    vm.totalItems = 150;
    vm.currentPage = 1;
    var superappid = $state.params.superappid;
    var appid = $state.params.appid;
    var userid = $state.params.userid;
    //vm.table1Throbber = true;
    //console.log(vm.table1Throbber);
    postData.fields = getJSONKeysArray(translogdashboard_table_md[tableName].dataKeys);

    function getJSONKeysArray(data){
      var arr = [];
      angular.forEach(data,function(value,key){
        arr.push(value.jsonkey);
      })
      return arr;
      //console.log("data is ",data);
      //console.log(arr);
    }
    //postData.offset  = 10;
    //postData.timestamp = Number.MAX_SAFE_INTEGER;

    function minTimeStamp(ts){
      return Math.min(...ts);
    }

    postData.limit = 2;
    postData.user_id = userid || "6300056568";
    postData.super_app = superappid || "d0bb80aa-bb86-39b6-a351-13f02e72752b";
    postData.token = "672c7c32-d764-11e8-b211-c37a4c089afb";
    postData.app_id = appid || "891997e8-6938-3d75-9693-ac62959be1ed";
    postData.page_no = 1;
    //console.log(postData);

    getTableData();
    //console.log(translogdashboardTable_table_md[tableName].dataKeys);

    // vm.data = translogdashboardDataService.getData();


    function getTableData(){
      //console.log("post:",postData);
      translogdashboardHttpService.getTableData()
      .save(postData,function(data){
        var data = angular.fromJson(angular.toJson(data.result.content));
        // console.log(data);
        var ts =   data.transactions.map( obj => obj.insert_ts);
        //postData.timestamp =  minTimeStamp(ts)
        //console.log("data is ",data);
        vm.tableData = translogdashboardDataService.formatTable(data.transactions,tableName);
      });
    }
    //pagination
    vm.pageChanged = function(){
      var pageNumber = vm.currentPage;
      console.log("pageNumber",pageNumber);
      postData.page_no = pageNumber;
      getTableData();
      return pageNumber;
    }


  }
})();
