(function (){
  angular
  .module('detailModule')
  .controller("projectdetailCtrl", projectdetailCtrl)
  .controller("imageCtrl", imageCtrl);

  projectdetailCtrl.$inject=['$scope','$window' ,'$state', '$location','image_table_md','$http', 'projectdetailService', 'cookieService','LOGIN_CONSTANTS','superappmap'];

  imageCtrl.$inject = ['$scope' ,'$state', '$location','image_table_md', '$http', 'projectdetailService', 'cookieService','LOGIN_CONSTANTS','superappmap','awsbuckets'];

  function projectdetailCtrl($scope, $window,$state, $location, $http , image_table_md , projectdetailService, cookieService,LOGIN_CONSTANTS,superappmap){
    var vm = this;

   $scope.loading = true;
   vm.projectList = [];
   vm.fields = [];
    var appid = $state.params.appid;
    var projectData ={};
    var project ;
    vm.table1Throbber = true

    projectData.superapp = $state.params.superappid;

    projectData.appid = $state.params.appid;

    projectData.projectid = $state.params.projectid;

    projectData.startDate = $state.params.startDate;

    projectData.endDate = $state.params.endDate

    projectData.token = superappmap.TOKEN;

    var tableName = "imageDataTable";

    $scope.data = null;
    $scope.itemsPerPage = 10;

    vm.submitImage = submitImage;
    vm.finalArr = [];

    $scope.pagination = {
      currentPage:1,
      maxSize :1,
      totalItems:0
    }

    $scope.$watch("pagination.currentPage",function(){
      setPagingData($scope.pagination.currentPage);
    });

    function setPagingData(page){

      if(vm.finalArr.length > 0){
      var pageData = vm.finalArr.slice((page - 1) * $scope.itemsPerPage,
      page * $scope.itemsPerPage
      );
    }
    if(vm.finalArr.length >= 10){
      var count = (vm.finalArr.length) / 10 ;
      count = parseInt(count) +1;
      $scope.pagination.totalItems = count;
    }else{
      $scope.pagination.totalItems = 1;
    }
      // $scope.pagination.totalItems = vm.finalArr.length;
      vm.tableData = projectdetailService.formatTable( pageData,tableName)

    }

    projectdetailService.getProjectDetail().save(projectData,function(Data){
      // console.log(Data);
        getTableData(Data);

    });

   function getTableData(data){
     var finalData = angular.fromJson(angular.toJson(data));
     vm.finalArr = [];
     angular.forEach(finalData, function(value,key){
        var root = {};
        var count = 0;
        root.image = [];
        root.date = key;
        root.timestamp = key;
        root.formdata = value;
        angular.forEach(value,function(val,keyval){
          keyval = keyval.toString();
          console.log(typeof keyval, keyval);
          if(keyval.indexOf(superappmap.IMAGEDATATYPE) != -1 || keyval.indexOf(superappmap.IMAGEDATA) != -1 || keyval.indexOf(superappmap.IMAGETYPE) != -1){
            console.log("coming into if ",superappmap.IMAGEDATATYPE);
            var imagehash = [];
            imagehash = val.split(",");
            for(i=0;i<imagehash.length;i++){
              var single = [];
              single = imagehash[i].split(" ");
              root.image.push(single[0]);
            }
          }
        });

        vm.finalArr.push(root);
   });
   setPagingData($scope.pagination.currentPage);
  vm.tableData = projectdetailService.formatTable(vm.finalArr,tableName);
  console.log(vm.tableData)

}

  function submitImage(data){
      console.log(data);
      var image = data.split("##");
      console.log(image);
      var lat = image[1].replace('.', '@');
      var lng = image[2].replace('.', '@');
      var url =  $state.go('imagefile',{'superappid':projectData.superapp,'appid':projectData.appid, 'projectid':projectData.projectid, 'imageid':image[0],'lat':lat,'lng':lng});

  }
}

function imageCtrl($scope, $state, $location, $http , image_table_md , projectdetailService, cookieService,LOGIN_CONSTANTS,superappmap,awsbuckets){
    var vm = this;
    var imageData  = {};
    console.log("This is map controller");

    var image = []
    var aws = "";
    console.log(awsbuckets);
    var buckets = (!!awsbuckets && !!awsbuckets.awsbuckets) ? awsbuckets.awsbuckets : [];
    for(var i=0;i< buckets.length;i++){
      if(buckets[i].superappid == $state.params.superappid){
        aws = buckets[i].awsurl;
      }
    }
    console.log(aws);

    var s3url =  aws+$state.params.appid+"$$"+$state.params.projectid+"$$"+$state.params.imageid;
    console.log("actual url");
    console.log(s3url);
    // var s3url  = "https://s3-us-west-2.amazonaws.com/uniapp-test/6bf61cb2-d659-3b93-950b-59075ce434df%24%24ecfda2e3-3a5f-11e9-8f0f-ef96be18a74f%24%24a7682278-59ba-4db3-9e16-ba1d27c751e4"
    // console.log(s3url);

    imageData.userid = superappmap.USER;
    imageData.token = superappmap.TOKEN;

      var lat = $state.params.lat;
      var lng = $state.params.lng;

      if(!!lat){
        lat = lat.replace('@','.');
      }
      if(!!lng){
        lng = lng.replace('@', '.');
      }
      console.log(lat, lng);
      var mymap = L.map('mapid').setView([lng, lat], 13);

      L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(mymap);

      L.marker([lng, lat]).addTo(mymap)
    .bindPopup('<img src= "'+s3url+'" height="250px" width="250px" align="center" data-err-src="images/png/avatar.png">')
    .openPopup();

}

})();
