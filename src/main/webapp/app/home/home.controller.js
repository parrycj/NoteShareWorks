(function () {
    'use strict';

    angular
        .module('noteShareApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', 'LoginService', '$state', 'Note', 'NoteSearch', 'CourseSearch'];

    function HomeController($scope, Principal, LoginService, $state, Note, NoteSearch, CourseSearch) {
        var vm = this;

        vm.account = null;
        vm.isAuthenticated = null;
        vm.login = LoginService.open;
        vm.register = register;
        vm.search = search;
        vm.searchByClass = searchByClass;
        vm.loadAll = loadAll;
        $scope.$on('authenticationSuccess', function () {
            getAccount();

        });

        function loadAll() {
            Note.query(function (result) {
                vm.notes = result;
            });
        }

        getAccount();

        function getAccount() {
            Principal.identity().then(function (account) {
                vm.account = account;
                vm.isAuthenticated = Principal.isAuthenticated;
            });
        }

        function register() {
            $state.go('register');
        }

        function search() {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            NoteSearch.query({query: vm.searchQuery}, function (result) {
                console.log(result.toString() + vm.searchQuery.toString());
                vm.notes = result;
                $location.url('/note');
            });
        }

        function searchByClass() {
            CourseSearch.query({query: vm.searchQuery2}, function (result) {
                vm.courses = result;
                $location.url('/course/1');
            });
        }

    }
})();
