(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('SchoolController', SchoolController);

    SchoolController.$inject = ['$scope', '$state', 'School', 'SchoolSearch'];

    function SchoolController ($scope, $state, School, SchoolSearch) {
        var vm = this;
        
        vm.schools = [];
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            School.query(function(result) {
                vm.schools = result;
            });
        }

        function search () {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            SchoolSearch.query({query: vm.searchQuery}, function(result) {
                vm.schools = result;
            });
        }    }
})();
