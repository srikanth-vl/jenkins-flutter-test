angular
    .module('cookieModule', [
        'ngCookies'
    ])
    .factory('cookieService', cookieService);
cookieService.$inject = ['$cookies'];
function cookieService($cookies) {
    var services = {
        get: get,
        put: put,
        remove: remove,
        removeAll: removeAll
    }

    return services;

    function get(key) {
        if ($cookies.get(key) !== undefined)
            return JSON.parse($cookies.get(key));
        else {
            return undefined;
        }
    }

    function put(key, value) {
        $cookies.put(key, JSON.stringify(value))
    }

    function remove(key) {
        $cookies.remove(key);
    }

    function removeAll(){
      var cookies = $cookies.getAll();
      angular.forEach(cookies, function(v, k) {
        $cookies.remove(k);
      });
    }
}
