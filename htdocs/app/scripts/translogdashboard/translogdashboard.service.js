angular
.module("translogdashboard")
.service("translogdashboardDataService", translogdashboardDataService)
.service("translogdashboardHttpService", translogdashboardHttpService);
translogdashboardDataService.$inject=['$resource', 'URL', 'vassarTableFormatService', 'translogdashboard_table_md'];
translogdashboardHttpService.$inject=['$resource', 'URL', '$http'];

function translogdashboardDataService($resource, URL, vassarTableFormatService, translogdashboard_table_md){
  var service={
    formatTable : formatTable
  };

  return service;

  function formatTable(data, tableType){
    var tableDataConstants = translogdashboard_table_md[tableType];
    return vassarTableFormatService.formatTable(data,tableDataConstants)
  }
}


function translogdashboardHttpService($resource, URL, $http){
  var service ={
    getTableData : getTableData
  };

  return service;

  function getTableData(){
    var result = $resource(URL.API_URL+"uniapp/transactionlog", {}, {
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
