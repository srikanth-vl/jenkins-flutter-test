(function() {
angular
    .module('formApp',['ui.bootstrap'])
    .directive('formDirective', formDirective)

function formDirective() {
    var directive = {
        restrict: 'EAC',
        templateUrl: 'scripts/lib/formdirective/form.html',
        link: linkFunc,
        scope: {
            data: "=data",
            throb:"=throb",
            download: "=download",
            formId :"@",
            onSubmit: '&onSubmit'
        },
        controller: 'formDirectiveController',
        controllerAs: 'vm'

    };
    return directive;

    function linkFunc(scope, element, attrs) {
    }
}
})();
