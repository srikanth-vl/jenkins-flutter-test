(function (){
  angular
  .module('imageCount')
  .controller("imageCountCtrl", imageCountCtrl);
  imageCountCtrl.$inject=['$scope','$state','$location','$http','imageCountDataService', 'imageCount_table_md', 'imageCountHttpService','cookieService','LOGIN_CONSTANTS','$window'];
  function imageCountCtrl($scope, $state, $location, $http, imageCountDataService, imageCount_table_md, imageCountHttpService,cookieService,LOGIN_CONSTANTS,$window){

    var vm = this;
    vm.filterSubmit = filterSubmit;
    vm.handlePopup = handlePopup;
    $scope.datecheck = 0;

    var maxtime;
    var mintime;
    var postData = {};
    var superappid = $state.params.superappid;
    var appid = $state.params.appid;
    var datestart;
    var dateend ;
    var tableName = "imageCountTable";
    vm.table1Throbber = true;
    vm.finalArr = [];
    if (!cookieService.get(LOGIN_CONSTANTS.SESSION_ID)){
        $state.go('uniapp');
     }
    vm.tableData = {};

    vm.currentPage = 1;
    vm.itemsPerPage = 10;
    vm.pagination = {
      currentPage:1,
      maxSize :1,
      totalItems:0
    }

    $scope.$watch("vm.pagination.currentPage",function(){
      setPagingData(vm.pagination.currentPage);
    });

    function setPagingData(page){
      if(vm.finalArr.length > 0){
      var pageData = vm.finalArr.slice((page - 1) * vm.itemsPerPage,
      page * vm.itemsPerPage
      );
      if(vm.finalArr.length >= vm.itemsPerPage){

        var count = (vm.finalArr.length) / vm.itemsPerPage;
        count = parseInt(count) +1;
        vm.pagination.totalItems = count;
      } else{
        vm.pagination.totalItems = 1;
      }

    }
      vm.tableData = imageCountDataService.formatTable(pageData,tableName);
    }

    function filterSubmit(response){
        dateend = imageCountHttpService.getDate(response.endsAt);
        var edate = new Date(response.endsAt);
        datestart = imageCountHttpService.getDate(response.startsAt);
        maxtime = response.endsAt.getTime()+86400000;
        var currentDate = response.startsAt;
        var d = currentDate.getFullYear()+"-"+(currentDate.getMonth()+1)+"-"+currentDate.getDate();
        mintime = new Date(d).getTime();
        superappid = response.superappid,
        appid = response.appid,
        getTableData();
    }

    getTableData();

    function getTableData(){

      postData.super_app = superappid ;
      postData.app_id = appid ;
      postData.start_ts = mintime;
      postData.end_ts = maxtime;

      imageCountHttpService.getTableData()
      .save(postData,function(data){
        vm.finalArr = [];
        var data = angular.fromJson(angular.toJson(data.result.content));
        angular.forEach(data, function(value, key){
          var res = flattenObject(value);
          vm.finalArr.push(res);
        });
        setPagingData(vm.pagination.currentPage);

      });
    }

    function handlePopup(data){
      var projectid = data.row.value;
      var url = $state.href('details',{'superappid':superappid,'appid':appid,'projectid':projectid,'startDate':datestart,'endDate':dateend});
      $window.open(url,'_blank');
    }

    function flattenObject(ob) {
      var toReturn = {};
      for(var i in ob) {
        if(!ob.hasOwnProperty(i)) continue;
        if((typeof ob[i]) == 'object') {
          var flatObject = flattenObject(ob[i]);
          for(var x in flatObject) {
            if(!flatObject.hasOwnProperty(x)) continue;
            toReturn[i + '.' + x] = flatObject[x];
          }
        } else {
          toReturn[i] = ob[i];
        }
      }
      return toReturn;
    };


    vm.multipart= imageCountDataService.getFormCons();


  }
})();
