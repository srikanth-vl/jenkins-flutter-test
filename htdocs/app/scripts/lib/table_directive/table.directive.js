(function() {
angular
    .module('vassarTable',['ngExportExcel'])
    .directive('vassarTableDirective', vassarTableDirective)
    .filter('positive',positiveFilter)
    .filter('removeDoubleQuotes',removeDoubleQuotes)
    .filter('dataFormatter',dataFormatter);
function removeDoubleQuotes(){
  return function (input) {
      if (!input || input === null || input === "-") {
          return input;
      }

      return input.toString().replace(/"/g,"");
  };
}
function positiveFilter() {

     return function (input) {
         if (!input) {
             return input;
         }
         return Math.abs(input);
     };
}
function vassarTableDirective() {
    var directive = {
        restrict: 'EAC',
        templateUrl: 'scripts/lib/table_directive/table.html',
        link: linkFunc,
        scope: {
            data: "=data",
            start: "=start",
            throb:"=throb",
            thColor: "=thColor",
            breadCum: "=breadCum",
            download: "=download",
            downloadtype: "@downloadtype",
            downloadfilename: "@",
            tableId :"@",
            serialNumberSuppress : "=",
            callback: '&callback'
        },
        controller: 'vassarTableDirectiveController'

    };
    return directive;

    function linkFunc(scope, element, attrs) {
      scope.parseFloat = parseFloat;
      scope.FormatNumber = function(input){
        if(isFloat(input)){

            var number = input.toFixed(2);
        }else {
          var number = input;
        }
        return number.toLocaleString('en-IN');
      }

      function isFloat(n){
        return Number(n) === n && n % 1 !== 0;
      }
      scope.isNumber = angular.isNumber;
      scope.getAlphabet = function(index) {
        return String.fromCharCode(65+index);
      };
    }
}

function dataFormatter(){
  return function(input){
      // console.log(input);
    //input= input.data;

    //output = formatteddata(input);
      // console.log("input",input);
      var obj = JSON.stringify(input);
// console.log(obj);
var obj1 = obj.replace(/\[/g,'');
obj1 = obj1.replace(/\]/g,'');
obj1 = obj1.replace(/:/g,'=');
obj1 = obj1.replace(/{/g,'[');
obj1 = obj1.replace(/}/g,']');
obj1 = obj1.replace(/\"/g,'');

  return obj1;

  //console.log(outputArray);

  };

}


})();
