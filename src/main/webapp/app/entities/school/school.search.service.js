(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .factory('SchoolSearch', SchoolSearch);

    SchoolSearch.$inject = ['$resource'];

    function SchoolSearch($resource) {
        var resourceUrl =  'api/_search/schools/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
