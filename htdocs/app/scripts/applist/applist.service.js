angular
.module("applistModule")
.service("applistService",applistService);
applistService.$inject=['$resource', 'URL','$http'];

function applistService($resource, URL,$http){
  var service ={
    getForms : getForms
  };

  return service;

  function getForms(){
    var result = $resource(URL.API_URL+"uniapp/rootconfigdata", {}, {
      'save':{
        method:'POST',
        isArray:false,
        headers: {
          'Content-Type':'application/json'
        }
      }
    });
    return result;
  }
}
