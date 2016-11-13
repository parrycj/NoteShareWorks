(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('ProfessorController', ProfessorController);

    ProfessorController.$inject = ['$scope', '$state', 'Professor', 'ProfessorSearch'];

    function ProfessorController ($scope, $state, Professor, ProfessorSearch) {
        var vm = this;
        
        vm.professors = [];
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            Professor.query(function(result) {
                vm.professors = result;
            });
        }

        function search () {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            ProfessorSearch.query({query: vm.searchQuery}, function(result) {
                vm.professors = result;
            });
        }    }
})();
