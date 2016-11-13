(function () {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('NoteController', NoteController);

    NoteController.$inject = ['$scope', '$state', 'DataUtils', 'Note', 'NoteSearch'];

    function NoteController($scope, $state, DataUtils, Note, NoteSearch) {
        var vm = this;

        vm.notes = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            Note.query(function (result) {
                vm.notes = result;
            });
        }

        function search() {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            NoteSearch.query({query: vm.searchQuery}, function (result) {
                vm.notes = result;
            });
        }
    }

})();




