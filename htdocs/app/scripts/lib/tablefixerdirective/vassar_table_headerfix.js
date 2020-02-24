(function() {
    angular
    .module('vassarTableHeader',['jQueryScrollbar'])
    .directive('vassarTableHeaderFixer',vassarTableHeaderFixer);
    vassarTableHeaderFixer.$inject = ['$window','$timeout','$compile'];
    function vassarTableHeaderFixer($window,$timeout,$compile){
      var directive = {
        restrict:"EA",
        scope : {
          tableId : '@',
          headerFix : "=",
          columnFix : "=",
          numOfColumns : "=",
          watchOnElement : "="
        },
        transclude: true,
        template: '<div ng-transclude></div>',
        link : linkFun
      }
      function linkFun(scope, element, attrs, ctrl){
          var tableId = "#"+scope.tableId;
          var numOfColumns = angular.copy(scope.numOfColumns) || 0;
          var headerFix =  angular.copy(scope.headerFix) || false;
          var columnFix = angular.copy(scope.columnFix) || false;
          var watchOnElement = angular.isDefined(scope.watchOnElement)?scope.watchOnElement : true;
          var columnsWatch,headerWatch,elementContent,domWatch;
          elementContent = element.get(0);
          function setHeaderFix(){
              var fixObject  = {};
              fixObject.head = headerFix;
              if(angular.isDefined(columnFix) && columnFix){
                  fixObject.left = numOfColumns;
              }
              var parentElement = angular.element(tableId).parent();
              if(parentElement && parentElement !=null){
                parentElement.addClass("table-responsive");
                parentElement.addClass("scrollbar-macosx");
                parentElement.addClass("max-table-heightvh");
                parentElement.scrollbar();
                angular.element(tableId).tableHeadFixer(fixObject);
              }

          }

          columnsWatch = scope.$watch("numOfColumns",function(newValue,oldValue){
                numOfColumns = newValue;
                setHeaderFix();
          });
          headerWatch = scope.$watch("headerFix",function(newValue,oldValue){
                headerFix = newValue;
                setHeaderFix();
          });
          angular.element($window).bind("resize",setHeaderFix());
          if(watchOnElement){
            console.log("coming into watch on element")
            domWatch = scope.$watch(function () { return element.html(); },function (newValue, oldValue) {
               if (newValue !== oldValue) {
                 setHeaderFix();
            }});
          }else{
            console.log("watch is not defiend")
          }

          scope.$on('$destroy',function(){
            console.log("destroyed state");
            if(columnsWatch){
              columnsWatch();
              columnsWatch = null;
            }
            if(headerWatch){
              headerWatch();
              headerWatch = null;
            }
            if(domWatch){
              domWatch();
              domWatch = null;
            }
          });
      }
      return directive;
    }

})();
