(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('professor', {
            parent: 'entity',
            url: '/professor',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'noteShareApp.professor.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/professor/professors.html',
                    controller: 'ProfessorController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('professor');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('professor-detail', {
            parent: 'entity',
            url: '/professor/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'noteShareApp.professor.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/professor/professor-detail.html',
                    controller: 'ProfessorDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('professor');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Professor', function($stateParams, Professor) {
                    return Professor.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'professor',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('professor-detail.edit', {
            parent: 'professor-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/professor/professor-dialog.html',
                    controller: 'ProfessorDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Professor', function(Professor) {
                            return Professor.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('professor.new', {
            parent: 'professor',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/professor/professor-dialog.html',
                    controller: 'ProfessorDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                firstName: null,
                                lastName: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('professor', null, { reload: 'professor' });
                }, function() {
                    $state.go('professor');
                });
            }]
        })
        .state('professor.edit', {
            parent: 'professor',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/professor/professor-dialog.html',
                    controller: 'ProfessorDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Professor', function(Professor) {
                            return Professor.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('professor', null, { reload: 'professor' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('professor.delete', {
            parent: 'professor',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/professor/professor-delete-dialog.html',
                    controller: 'ProfessorDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Professor', function(Professor) {
                            return Professor.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('professor', null, { reload: 'professor' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
