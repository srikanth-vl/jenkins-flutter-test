angular
.module("projectlistModule")
.service("projectlistService",projectlistService);
projectlistService.$inject=['$resource', 'URL'];

function projectlistService($resource, URL){
  var service ={
    getAppForms : getAppForms,
    getProjects : getProjects

  };

  return service;

  function getAppForms(){
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

  function getProjects()
  {
    var result = $resource(URL.API_URL+"uniapp/projectlist", {}, {
      'save':{
        method:'POST',
        isArray:false,
        headers: {
          'Content-Type':'application/json'
    }
  }
})
  return result;
}
}
