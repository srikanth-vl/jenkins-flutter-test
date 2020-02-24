angular
.module("superapplistModule")
.service("superapplistService",superapplistService);
superapplistService.$inject=['$resource', 'URL'];

function superapplistService($resource, URL){
  var service ={
    getForms : getForms
  };

  return service;

  function getForms(){
    var result = $resource(URL.API_URL+"uniapp/projecttype", {}, {
      'save':{
        method:'POST',
        isArray:false,
        headers: {
          'Content-Type':'application/json'
        }
      }
    });
    // console.log(result);
    return result;
  }
}
