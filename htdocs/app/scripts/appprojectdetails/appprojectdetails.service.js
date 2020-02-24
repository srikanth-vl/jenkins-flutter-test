angular
.module('detailModule')
.service("projectdetailService",projectdetailService);

projectdetailService.$inject=['$resource', 'URL','vassarTableFormatService', 'image_table_md'];

function projectdetailService($resource,URL,vassarTableFormatService, image_table_md){
  var service = {
    formatTable : formatTable,
    getProjectDetail:getProjectDetail,
    getImageDetail : getImageDetail
  };

  return service;

  function formatTable(data, tableType){
    var tableDataConstants = image_table_md[tableType];
    return vassarTableFormatService.formatTable(data,tableDataConstants)
  }

  function getProjectDetail(){
    var result = $resource(URL.API_URL+"uniapp/projectdetail", {}, {
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

  function getImageDetail(){
    var result = $resource(URL.API_URL + "uniapp/imagedetail",{},{
      'save':{
         method : 'POST',
         isArray : false,
         headers: {
            'Content-Type': 'application/json'
         }
      }
    });

    return result;
  }

  }
