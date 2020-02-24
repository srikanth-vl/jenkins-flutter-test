angular
.module("uniapp")
.service("uniappDataService",uniappDataService);
uniappDataService.$inject=['$resource', 'URL'];

function uniappDataService($resource, URL){
  var service ={
    // getSuperAppMetaConfig : getSuperAppMetaConfig,
    getAppConfigurationList: getAppConfigurationList
  };
  
  return service;
  // function getSuperAppMetaConfig(){
  //   var result = $resource(URL.API_URL+"uniapp/appmetaconfigjson", {}, {
  //     'save':{
  //       method:'POST',
  //       isArray:false,
  //       headers: {
  //         'Content-Type':'application/json'
  //       }
  //     }
  //   });
  //   // console.log(result);
  //   return result;
  // }

  function getAppConfigurationList(){
    var result = $resource(URL.API_URL+"uniapp/rootconfigdata", {}, {
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
