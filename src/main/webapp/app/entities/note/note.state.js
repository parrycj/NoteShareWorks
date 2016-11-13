(function() {
    'use strict';

    angular
        .module('noteShareApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('note', {
            parent: 'entity',
            url: '/note',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'noteShareApp.note.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/note/notes.html',
                    controller: 'NoteController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('note');
                    $translatePartialLoader.addPart('privacy');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('note-detail', {
            parent: 'entity',
            url: '/note/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'noteShareApp.note.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/note/note-detail.html',
                    controller: 'NoteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('note');
                    $translatePartialLoader.addPart('privacy');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Note', function($stateParams, Note) {
                    return Note.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'note',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('note-detail.edit', {
            parent: 'note-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/note/note-dialog.html',
                    controller: 'NoteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Note', function(Note) {
                            return Note.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('note.new', {
            parent: 'note',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/note/note-dialog.html',
                    controller: 'NoteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                noteContent: null,
                                noteContentContentType: null,
                                dateSubmitted: null,
                                overallRating: null,
                                handwritingRating: null,
                                usefulnessRating: null,
                                privacy: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('note', null, { reload: 'note' });
                }, function() {
                    $state.go('note');
                });
            }]
        })
        .state('note.edit', {
            parent: 'note',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/note/note-dialog.html',
                    controller: 'NoteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Note', function(Note) {
                            return Note.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('note', null, { reload: 'note' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('note.delete', {
            parent: 'note',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/note/note-delete-dialog.html',
                    controller: 'NoteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Note', function(Note) {
                            return Note.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('note', null, { reload: 'note' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
