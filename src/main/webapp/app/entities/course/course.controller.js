(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('CourseController', CourseController);

    CourseController.$inject = ['$scope', '$state', 'Course', 'CourseSearch'];

    function CourseController ($scope, $state, Course, CourseSearch) {
        var vm = this;
        
        vm.courses = [];
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            Course.query(function(result) {
                vm.courses = result;
            });
        }

        function search () {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            CourseSearch.query({query: vm.searchQuery}, function(result) {
                vm.courses = result;
            });
        }    }
})();
