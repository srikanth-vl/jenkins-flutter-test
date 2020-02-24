(function() {
  'use strict';
  angular
      .module("formApp")
      .service("formHttpService",formHttpService)
      .service("formDataService", formDataService);

  formHttpService.$inject = ['$resource','URL','$http'];
  formDataService.$inject = ['$q', '$filter', 'formconstants', 'formHttpService', '$state','superapp'];

  function formDataService( $q, $filter, formconstants, formHttpService, $state,superapp){
    var service = {
      prepareFormJsonData : prepareFormJsonData,
      getsuperappvalues   : getsuperappvalues
    }
    return service;

    function getsuperappvalues(){
      return superapp['superappvalues'];
    }

    function prepareFormJsonData(data){
      var finaldata = {};
      var dropdownlists = {};
      angular.forEach(data, function(value, key){
        finaldata[key] = value;
        if(key === formconstants.FIELDS){
          var fieldsData = {};
          angular.forEach(value, function(value1, key1){
            var obj = {};
            //LABEL --> The only required attribute
            obj[formconstants.LABEL] = value1[formconstants.LABEL];
            //KEY --> optional. If the key is not present we use the label to generate it. Convert the label to lowercase and replace spaces with "_"
            obj[formconstants.KEY] = value1[formconstants.KEY] ? value1[formconstants.KEY] : getConcatenatedValue(value1[formconstants.LABEL]);
            //MANDATORY --> Default - true
            if(value1[formconstants.MANDATORY] ||  value1[formconstants.MANDATORY] == false){
              obj[formconstants.MANDATORY] =  value1[formconstants.MANDATORY];
            }
            else{
              obj[formconstants.MANDATORY] =  true;
            }
            //PLACEHOLDER - Label by default unless specified
            obj[formconstants.PLACEHOLDER] = value1[formconstants.PLACEHOLDER] ? value1[formconstants.PLACEHOLDER] : obj[formconstants.LABEL];
            //TYPE - type of data - text, number, password, dropdown, radio, etc. Default - text
            obj[formconstants.TYPE] =  value1[formconstants.TYPE] ? value1[formconstants.TYPE] : formconstants.TEXT;
            //DATATYPE - type of formfield - double, string, integer, etc. Sent to backend. Default - String
            obj[formconstants.DATATYPE] = value1[formconstants.DATATYPE] ? value1[formconstants.DATATYPE] : formconstants.STRING;
            //MINLENGTH -  Specifies the minimum length of the form field input.
            //MAXLENGTH -  Specifies the maximum length of the form field input.
            if(obj[formconstants.TYPE] === formconstants.TEXT || obj[formconstants.TYPE] === formconstants.PASSWORD ||
               obj[formconstants.TYPE] === formconstants.NUMBER || obj[formconstants.TYPE] === formconstants.EMAIL){
              obj[formconstants.MINLENGTH] = (value1[formconstants.MINLENGTH] && value1[formconstants.MINLENGTH] > value1[formconstants.MAXLENGTH]) ? value1[formconstants.MINLENGTH] : "";
              obj[formconstants.MAXLENGTH] = value1[formconstants.MAXLENGTH] ? value1[formconstants.MAXLENGTH] : "";
            }
            /**
             *  Components like dropdown, radio button and checkboxes all require an list(arraylist) or an object(hashmap).
             *  We can provide the data for these components in the following ways -
             *  1. From CONFIG - as a list or object
             *  2. From BACKEND - as a list(arraylist) or an object(hashmap). Hence, we make two different calls, one to get data as an array(isarray = true),
             *                    other to get data as an object(isarray=false). The URL2 is specified in the listsource in the configuration.
             *
             **/
            if(value1[formconstants.TYPE] === formconstants.DROPDOWN || value1[formconstants.TYPE] === formconstants.RADIOBUTTON ||
              value1[formconstants.TYPE] === formconstants.CHECKBOX){
              value1[formconstants.LISTSOURCE]  = value1[formconstants.LISTSOURCE] ? value1[formconstants.LISTSOURCE] : formconstants.FROMCONFIG;
              if(value1[formconstants.CHANGE]){
                obj.change = value1[formconstants.CHANGE];
                obj.child = value1[formconstants.CHILD];
              }
              if(value1[formconstants.DEPENDENT]){
                obj.dependent = value1[formconstants.DEPENDENT];
                obj.parent = value1[formconstants.PARENT];
              }
              if(value1[formconstants.LISTSOURCE] === formconstants.FROMCONFIG){
                obj.dropdownlist = value1[formconstants.LISTARRAY];
                dropdownlists[value1[formconstants.KEY]] = value1[formconstants.LISTARRAY];
              }
              else if(value1[formconstants.LISTSOURCE] === formconstants.FROMBACKEND && !value1[formconstants.DEPENDENT]){
                var listArrayUrl = value1[formconstants.LISTARRAYURL];
                var postdata = {};
                var stateParams = $state.params;
                if(stateParams){
                  angular.forEach(stateParams, function(value, key){
                    postdata[key] = value;
                  });
                }
                if(value1[formconstants.ISARRAY] == true){
                  formHttpService.getDataAsArray(listArrayUrl)
                    .save(postdata,function(data){
                      data = angular.fromJson(angular.toJson(data));
                      if(value1[formconstants.TYPE] === formconstants.DROPDOWN){
                        dropdownlists[value1[formconstants.KEY]] = data;
                      }
                      else{
                        obj.dropdownlist = data;
                      }
                  });
                }
                else if(value1[formconstants.ISARRAY] == false){
                  formHttpService.getDataAsObject(listArrayUrl)
                    .save(postdata,function(data){
                      data = angular.fromJson(angular.toJson(data));
                      if(value1[formconstants.TYPE] === formconstants.DROPDOWN){
                        dropdownlists[value1[formconstants.KEY]] = data;
                      }
                      else{
                        obj.dropdownlist = data;
                      }
                  });
                }
              }
              else{
                obj[formconstants.LISTARRAYURL] = value1[formconstants.LISTARRAYURL] ? value1[formconstants.LISTARRAYURL] : "";
                obj[formconstants.ISARRAY] = value1[formconstants.ISARRAY] ? value1[formconstants.ISARRAY] : "";
              }
              //SENDVALUE - sends key by default. specify "value" to send value.
              obj.sendvalue = value1[formconstants.SENDVALUE] ?  value1[formconstants.SENDVALUE] : formconstants.KEY;
            }
            fieldsData[obj.key] = obj;
            // fieldArray.push(obj);
            // console.log('fieldsData ',fieldsData);
          });
          finaldata[key] = fieldsData;
        }
      });
      finaldata["dropdownlists"] = dropdownlists;
      // console.log("finaldata is ",finaldata);
      return finaldata;
    }

    function getConcatenatedValue(data){
      if(!data)
        return;
      data = data.toLowerCase();
      data = data.trim();
      data = data.replace(" ", "_");
      return data;
    }
  }

  function formHttpService($resource,URL,$http){
    var service = {
      submitFormData  : submitFormData,
      submitMultiPartFormData : submitMultiPartFormData,
      getDataAsArray : getDataAsArray,
      getDataAsObject : getDataAsObject
    };
    return service;

    function submitFormData(submissionurl){
      var data = $resource(submissionurl,{},{
        'save':{
          method: 'POST',
          isArray : false,
          headers : {
            'Content-Type': 'application/json'
          }
        }
      });
      return data;
    }

    function submitMultiPartFormData(submissionurl, formdata, file){
      var fd = new FormData();
      if(file){
        fd.append("file", file);
      }
      if(formdata){
        fd.append("model", formdata);
      }
      $http.post(submissionurl, fd, {
          transformRequest: angular.identity,
          headers: {'Content-Type': undefined}
      }).then(function successCallback(response) {
        // console.log("response :: ",response);
        // this callback will be called asynchronously
        // when the response is available
      }, function errorCallback(response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
      });
      // .success(function(){
      // })
      // .error(function(){
      // });
    }

    function getDataAsArray(geturl){
      var data = $resource(geturl,{},{
        'save':{
          method: 'POST',
          isArray : true,
          headers : {
            'Content-Type': 'application/json'
          }
        }
      });
      return data;
    }

    function getDataAsObject(geturl){
      var data = $resource(geturl,{},{
        'save':{
          method: 'POST',
          isArray : false,
          headers : {
            'Content-Type': 'application/json'
          }
        }
      });
      return data;
    }
  }
}());
