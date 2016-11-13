(function() {
    'use strict';
    angular
        .module('noteShareApp')
        .factory('Professor', Professor);

    Professor.$inject = ['$resource'];

    function Professor ($resource) {
        var resourceUrl =  'api/professors/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
