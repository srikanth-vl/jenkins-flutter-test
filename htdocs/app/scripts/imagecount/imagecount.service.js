angular
.module("imageCount")
.service("imageCountDataService", imageCountDataService)
.service("imageCountHttpService", imageCountHttpService);
imageCountDataService.$inject=['$resource', 'URL', 'vassarTableFormatService', 'imageCount_table_md', 'filterConstant'];
imageCountHttpService.$inject=['$resource', 'URL', '$http'];

function imageCountDataService($resource, URL, vassarTableFormatService, imageCount_table_md, filterConstant){
  var service={
    formatTable : formatTable,
    getFormCons: getFormCons
  };

  return service;

  function getFormCons(){
    return filterConstant['multipartform'];
  }

  function formatTable(data, tableType){
    var tableDataConstants = imageCount_table_md[tableType];
    return vassarTableFormatService.formatTable(data,tableDataConstants)
  }
}


function imageCountHttpService($resource, URL, $http){
  var service = {
    getTableData : getTableData,
    getDate : getDate
    };

  return service;

  function getDate(dateval){
    var temp = dateval.getFullYear()*10000;
    var month = dateval.getMonth()+1;
    temp = temp + month*100;
    temp = temp + dateval.getDate();
    return temp;
  }

  function getTableData(){
    var result = $resource(URL.API_URL+"uniapp/imagedata", {}, {
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
