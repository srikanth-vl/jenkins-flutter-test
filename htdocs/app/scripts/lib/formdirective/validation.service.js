(function() {
  'use strict';

  angular
      .module("formApp")
      .service("validationService",validationService);

      validationService.$inject = ['$resource','URL','$http', '$q', '$filter', 'formconstants'];

      function validationService( $resource, URL, $http, $q, $filter, formconstants){
        var service = {
        }
        return service;
      }
}());
