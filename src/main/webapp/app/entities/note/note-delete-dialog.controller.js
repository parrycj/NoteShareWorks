(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('NoteDeleteController',NoteDeleteController);

    NoteDeleteController.$inject = ['$uibModalInstance', 'entity', 'Note'];

    function NoteDeleteController($uibModalInstance, entity, Note) {
        var vm = this;

        vm.note = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Note.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
