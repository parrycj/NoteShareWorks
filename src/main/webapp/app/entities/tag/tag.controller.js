(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('TagController', TagController);

    TagController.$inject = ['$scope', '$state', 'Tag', 'TagSearch'];

    function TagController ($scope, $state, Tag, TagSearch) {
        var vm = this;
        
        vm.tags = [];
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            Tag.query(function(result) {
                vm.tags = result;
            });
        }

        function search () {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            TagSearch.query({query: vm.searchQuery}, function(result) {
                vm.tags = result;
            });
        }    }
})();
