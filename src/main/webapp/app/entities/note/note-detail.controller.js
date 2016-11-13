(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('NoteDetailController', NoteDetailController);

    NoteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'Note', 'User', 'Course', 'Tag'];

    function NoteDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, Note, User, Course, Tag) {
        var vm = this;

        vm.note = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('noteShareApp:noteUpdate', function(event, result) {
            vm.note = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
