(function (){
  angular
  .module('superapplistModule')
  .controller("superapplistCtrl", superapplistCtrl);
  superapplistCtrl.$inject=['$scope','$timeout' ,'$state', '$location', '$http', 'uniappDataService', 'cookieService','LOGIN_CONSTANTS'];

  function superapplistCtrl($scope,$timeout ,$state, $location, $http, uniappDataService, cookieService,LOGIN_CONSTANTS){
    var url = $location.absUrl().split("/")[4];
    var vm = this;
    console.log(url);
    vm.decide ;
    if (cookieService.get(LOGIN_CONSTANTS.USER_ID) == 'admin' && url == 'admin'){
       vm.decide = 'admin';
    }
    else if(cookieService.get(LOGIN_CONSTANTS.USER_ID) != 'admin' && url == 'admin')
    {

      $state.go('uniapp');
    }
    else{
      vm.decide = 'user';
    }

    $scope.DisplayApps = function(superappid){
        if(vm.decide == 'user'){
          $state.go('userlogin',{'superappid':superappid});
        }
        if(vm.decide == 'admin'){
          $state.go('applist',{'superappid':superappid})
        }

    }

    $scope.fileUpload = function() {
        $state.go("fileupload")
    }
    // console.log("AWS started");
    // AWS.config.update({
    //   accessKeyId:'AKIAJNVL3G7MZO7M5TXA',
    //   secretAccessKey:'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'
    // });
    // AWS.config.region  = "us-west-2";
    //
    // var bucket = new AWS.S3({params: {Bucket: 'uniapp-test'}});
    //
    // console.log(bucket);
    //
    // bucket.getObject({Key: '03494406-0e7b-11e9-a47f-0947e4631ca5$$52ef9633-d88a-3480-b3a9-38ff9eaa2a25$$6f569a41-1fe1-11e9-91f2-1beccdc8c1d6$$2b19129f-efb5-4cf6-b317-f1e9faa59555'},function(err,file){
    //   console.log(file);
    // });
    //
    // function encode(data)
    // {
    // var str = data.reduce(function(a,b){ return a+String.fromCharCode(b) },'');
    // return btoa(str).replace(/.{76}(?=.)/g,'$&\n');
    // }

}

})();
